package org.example;

import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;

import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.ImageAnnotatorSettings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class VisionApiExample {

    public static ImageAnnotatorClient initializeVisionClient() throws IOException {
        ImageAnnotatorSettings imageAnnotatorSettings =
                ImageAnnotatorSettings.newBuilder().build();
        return ImageAnnotatorClient.create(imageAnnotatorSettings);
    }

    public VisionApiExample () {

    }

    public Image prepareImage(String filePath) throws IOException {
        System.out.println(filePath);
        ByteString imgBytes = ByteString.readFrom(Files.newInputStream(Paths.get(filePath)));
        return Image.newBuilder().setContent(imgBytes).build();
    }

    public String detectLabels(String filePath) throws IOException {
        //ImageAnnotatorClient vision = initializeVisionClient();
        try (ImageAnnotatorClient vision = initializeVisionClient()) {
            List<AnnotateImageRequest> requests = new ArrayList<>();
            Image img = prepareImage(filePath);

            Feature feat = Feature.newBuilder().setType(Feature.Type.LABEL_DETECTION).build();
            AnnotateImageRequest request =
                    AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
            requests.add(request);

            AnnotateImageResponse response = vision.batchAnnotateImages(requests).getResponsesList().get(0);

            if (response.hasError()) {
                System.out.printf("Error: %s\n", response.getError().getMessage());
                return "error";
            }


            response.getLabelAnnotationsList().forEach(label ->
                    System.out.printf("Label: %s\n", label.getDescription())
            );

            return DetectedLabelstoString(response.getLabelAnnotationsList());
        }
    }

    public String DetectedLabelstoString(List<EntityAnnotation> labels) {
        String feature = "";
        String pr = "\n";
        if (labels != null && !labels.isEmpty()) {
            for (EntityAnnotation label : labels) {
                feature = feature + label.getDescription() + pr;

            }
        } else {
            feature = "No";
        }
        return feature;
    }

}
