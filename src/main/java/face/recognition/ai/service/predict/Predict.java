package face.recognition.ai.service.predict;

import ai.djl.Model;
import ai.djl.ModelException;
import ai.djl.basicmodelzoo.basic.Mlp;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.modality.cv.translator.ImageClassificationTranslator;
import ai.djl.translate.TranslateException;
import ai.djl.translate.Translator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static face.recognition.ai.common.Arguments.HEIGHT;
import static face.recognition.ai.common.Arguments.WIDTH;

@Service
public class Predict {

    private static final Logger logger = LoggerFactory.getLogger(Predict.class);

    public Classifications predict(MultipartFile multipartFile) throws IOException, ModelException, TranslateException {
        Image img = ImageFactory.getInstance().fromInputStream(multipartFile.getInputStream());
        logger.info("Read image input stream");
        String modelName = "mlp";
        try (Model model = Model.newInstance(modelName)) {
            model.setBlock(new Mlp(HEIGHT * WIDTH, 10, new int[]{128, 64}));

            Path modelDir = Paths.get("C:\\Users\\user\\Desktop\\archive\\mlp");
            model.load(modelDir);

            List<String> classes =
                    IntStream.range(0, 10).mapToObj(String::valueOf).collect(Collectors.toList());
            Translator<Image, Classifications> translator =
                    ImageClassificationTranslator.builder()
                            .addTransform(new ToTensor())
                            .optSynset(classes)
                            .optApplySoftmax(true)
                            .build();

            try (Predictor<Image, Classifications> predictor = model.newPredictor(translator)) {
                return predictor.predict(img);
            }
        }
    }

}
