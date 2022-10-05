package face.recognition.ai.service.train;

import ai.djl.Model;
import ai.djl.basicdataset.cv.classification.ImageFolder;
import ai.djl.basicmodelzoo.basic.Mlp;
import ai.djl.engine.Engine;
import ai.djl.metric.Metrics;
import ai.djl.modality.cv.transform.Resize;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.ndarray.types.Shape;
import ai.djl.nn.Block;
import ai.djl.repository.Repository;
import ai.djl.training.DefaultTrainingConfig;
import ai.djl.training.EasyTrain;
import ai.djl.training.Trainer;
import ai.djl.training.TrainingResult;
import ai.djl.training.dataset.RandomAccessDataset;
import ai.djl.training.evaluator.Accuracy;
import ai.djl.training.listener.SaveModelTrainingListener;
import ai.djl.training.listener.TrainingListener;
import ai.djl.training.loss.Loss;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.Pipeline;
import ai.djl.translate.TranslateException;
import face.recognition.ai.common.Arguments;
import face.recognition.ai.common.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static face.recognition.ai.common.Arguments.*;

public class TrainModel {

    private static final Logger logger = LoggerFactory.getLogger(TrainModel.class);

    public static void main(String[] args) {
        try (Model model = buildModel(args)) {
            Path modelDir = Paths.get("C:\\Users\\user\\Desktop\\archive\\mlp");
            Files.createDirectories(modelDir);
            assert model != null;
            model.save(modelDir, "mlp");
            logger.info("Saved model");
        } catch (Exception e) {
            logger.error("Error occurred: " + e.getMessage());
        }
    }

    public static Model buildModel(String[] args) throws IOException, TranslateException {
        Arguments arguments = new Arguments().parseArgs(args);
        if (arguments == null) {
            return null;
        }

        // Construct neural network
        Block block =
                new Mlp(
                        HEIGHT * WIDTH,
                        NUM_CLASSES,
                        new int[]{516, 256});

        try (Model model = Model.newInstance("mlp")) {
            model.setBlock(block);

            // get training and validation dataset
            RandomAccessDataset trainingSet = getDataset(arguments).first;
            RandomAccessDataset validateSet = getDataset(arguments).second;

            // setup training configuration
            DefaultTrainingConfig config = setupTrainingConfig(arguments);

            try (Trainer trainer = model.newTrainer(config)) {
                trainer.setMetrics(new Metrics());

                Shape inputShape = new Shape(1, HEIGHT * WIDTH);

                // initialize trainer with proper input shape
                trainer.initialize(inputShape);

                EasyTrain.fit(trainer, arguments.getEpoch(), trainingSet, validateSet);

                trainer.getTrainingResult();
                return model;
            }
        }
    }

    private static DefaultTrainingConfig setupTrainingConfig(Arguments arguments) {
        String outputDir = arguments.getOutputDir();
        SaveModelTrainingListener listener = new SaveModelTrainingListener(outputDir);
        listener.setSaveModelCallback(
                trainer -> {
                    TrainingResult result = trainer.getTrainingResult();
                    Model model = trainer.getModel();
                    float accuracy = result.getValidateEvaluation("Accuracy");
                    model.setProperty("Accuracy", String.format("%.5f", accuracy));
                    model.setProperty("Loss", String.format("%.5f", result.getValidateLoss()));
                });
        return new DefaultTrainingConfig(Loss.softmaxCrossEntropyLoss())
                .addEvaluator(new Accuracy())
                .optDevices(Engine.getInstance().getDevices(arguments.getMaxGpus()))
                .addTrainingListeners(TrainingListener.Defaults.logging(outputDir))
                .addTrainingListeners(listener);
    }

    private static Pair<ImageFolder, ImageFolder> getDataset(Arguments arguments) throws IOException {
        String trainingDatasetRoot = "C:\\Users\\user\\Desktop\\archive\\car_data\\car_data\\train";
        String validateDatasetRoot = "C:\\Users\\user\\Desktop\\archive\\car_data\\car_data\\test";
        ImageFolder trainingDataset = initDataset(trainingDatasetRoot, arguments);
        ImageFolder validateDataset = initDataset(validateDatasetRoot, arguments);
        return new Pair<>(trainingDataset, validateDataset);
    }

    private static ImageFolder initDataset(String datasetRoot, Arguments arguments) throws IOException {
        Repository repository = Repository.newInstance("folder", Paths.get(datasetRoot));
        ImageFolder dataset = ImageFolder
                .builder()
                .setRepository(repository).optPipeline(
                        // create preprocess pipeline
                        new Pipeline()
                                .add(new Resize(WIDTH, HEIGHT))
                                .add(new ToTensor()))
                .setSampling(arguments.getBatchSize(), true)
                .optLimit(arguments.getLimit())
                .build();
        dataset.prepare(new ProgressBar());
        return dataset;
    }
}
