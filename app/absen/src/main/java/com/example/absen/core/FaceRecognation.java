package com.example.absen.core;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;

import com.example.absen.R;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.GpuDelegate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class FaceRecognation {

    private final Interpreter interpreter;
    private final int INPUT_SIZE;
    private CascadeClassifier cascadeClassifier;

    public FaceRecognation(AssetManager assetManager, Context context, String modelPath, int input_size) throws IOException{
        INPUT_SIZE = input_size;
        Interpreter.Options options = new Interpreter.Options();
        GpuDelegate gpuDelegate = new GpuDelegate();

        //Load Model
        options.setNumThreads(4);
        interpreter = new Interpreter(loadModel(assetManager,modelPath),options);

        //Load haar cacade model
        try {
            InputStream inputStream = context.getResources().openRawResource(R.raw.haarcascade_frontalface_alt);
            File cascadeDir = context.getDir("cascade",Context.MODE_PRIVATE);
            File mCascadeFile = new File(cascadeDir,"haarcascade_frontalface_alt.xml");
            FileOutputStream outputStream = new FileOutputStream(mCascadeFile);

            byte[] buffer = new byte[4096];
            int byteRead;
            while ((byteRead=inputStream.read(buffer)) != -1){
                outputStream.write(buffer,0,byteRead);
            }

            inputStream.close();
            outputStream.close();

            cascadeClassifier = new CascadeClassifier(mCascadeFile.getAbsolutePath());
        }catch (IOException e){
            Log.e("FaceRecognation",e.getMessage());
        }
    }

    public boolean recognizeImage(Mat mat_image, String faceRead){
        boolean result = false;

        if (faceRead != null){

            //Flip image to 90 derajat
            Core.flip(mat_image.t(),mat_image,1);

            //convert image to grayscale
            Mat grayscaleImgae = new Mat();
            Imgproc.cvtColor(mat_image,grayscaleImgae,Imgproc.COLOR_RGBA2GRAY);

            //set width and heigth

            int height = grayscaleImgae.height();
            int width = grayscaleImgae.width();

            //define minimum height and width of face in frame
            int absoluteFaceSize = (int) (height *0.1);
            MatOfRect faces = new MatOfRect();
            if (cascadeClassifier != null){
                cascadeClassifier.detectMultiScale(grayscaleImgae,faces,1.1,2,2,
                        new Size(absoluteFaceSize,absoluteFaceSize),new Size());
            }

            Rect[] faceArray = faces.toArray();

            for (Rect rect : faceArray){
                Imgproc.rectangle(mat_image,rect.tl(),rect.br(),new Scalar(0,255,0,255));

                Rect roi = new Rect((int) rect.tl().x, (int) rect.tl().y,
                        ((int) rect.br().x) - ((int) rect.tl().x),
                        ((int) rect.br().y) - ((int) rect.tl().y));

                Mat crop_rgb = new Mat(mat_image,roi);

                Bitmap bitmap;
                bitmap = Bitmap.createBitmap(crop_rgb.cols(), crop_rgb.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(crop_rgb, bitmap);

                Bitmap scaleBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false);
                ByteBuffer byteBuffer = convertBitmapToByteBuffer(scaleBitmap);

                float[][] face_value = new float[1][1];
                interpreter.run(byteBuffer, face_value);

                float read_face = (float) Array.get(Array.get(face_value, 0), 0);
                String read_face_ = String.format("%.2f", read_face);
                Log.d("read_face", read_face_);
                if (read_face_.equals(faceRead)) {
                    result = true;
                    break;
                }
            }

            Core.flip(mat_image.t(),mat_image,0);
        }
        return result;
    }

    private ByteBuffer convertBitmapToByteBuffer(Bitmap scaleBitmap) {
        ByteBuffer byteBuffer;
        int input_size = INPUT_SIZE;

        byteBuffer = ByteBuffer.allocateDirect(4 * input_size * input_size * 3);
        byteBuffer.order(ByteOrder.nativeOrder());
        int[] intValues = new int[input_size*input_size];
        scaleBitmap.getPixels(intValues,0,scaleBitmap.getWidth(),0,0,scaleBitmap.getWidth(),
                scaleBitmap.getHeight());

        int pixels = 0;
        for (int i = 0;i<input_size;i++){
            for (int j=0;j<input_size;j++){
                final int val = intValues[pixels++];
                byteBuffer.putFloat((((val>>16)&0xFF))/255.0f);
                byteBuffer.putFloat((((val>>8)&0xFF))/255.0f);
                byteBuffer.putFloat(((val & 0xFF))/255.0f);
                //it is placing RGB to MSB to LSB
            }
        }
        return byteBuffer;
    }

    private MappedByteBuffer loadModel(AssetManager assetManager, String modelPath) throws IOException {

        AssetFileDescriptor assetFileDescriptor = assetManager.openFd(modelPath);

        FileInputStream fileInputStream = new FileInputStream(assetFileDescriptor.getFileDescriptor());
        FileChannel fileChannel = fileInputStream.getChannel();
        long startOffset = assetFileDescriptor.getStartOffset();
        long declaredLength = assetFileDescriptor.getDeclaredLength();

        return fileChannel.map(FileChannel.MapMode.READ_ONLY,startOffset,declaredLength);
    }
}
