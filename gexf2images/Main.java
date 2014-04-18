import org.gephi.graph.api.*;
import org.gephi.io.exporter.api.*;
import org.gephi.io.exporter.preview.PNGExporter;
import org.gephi.io.importer.api.*;
import org.gephi.io.importer.api.Container;
import org.gephi.io.processor.plugin.*;
import org.gephi.plugins.layout.geo.*;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.project.api.*;
import org.gephi.ranking.api.*;
import org.gephi.ranking.plugin.transformer.*;
import org.openide.util.Lookup;

import java.awt.*;
import java.io.*;

public class Main {
    private Workspace getWorkspace() {
        ProjectController controller = Lookup.getDefault().lookup(ProjectController.class);
        controller.newProject();
        return controller.getCurrentWorkspace();
    }

    private void createGraph(String input, String output) {
        Workspace workspace = getWorkspace();
        importFileToWorkspace(input, workspace);
        doLayout();
        beautify();
        exportFile(output);
    }

    private void beautify() {
        RankingController controller = Lookup.getDefault().lookup(RankingController.class);
        Ranking degreeRanking = controller.getModel().getRanking(Ranking.NODE_ELEMENT, Ranking.DEGREE_RANKING);

        AbstractColorTransformer colorTransformer = (AbstractColorTransformer) controller.getModel().getTransformer(Ranking.NODE_ELEMENT, Transformer.RENDERABLE_COLOR);
        colorTransformer.setColorPositions(new float[]{0f, 0.2f});
        colorTransformer.setColors(new Color[]{new Color(0xD7191C), new Color(0x2C7BB6)});
        controller.transform(degreeRanking, colorTransformer);

        AbstractSizeTransformer sizeTransformer = (AbstractSizeTransformer) controller.getModel().getTransformer(Ranking.NODE_ELEMENT, Transformer.RENDERABLE_SIZE);
        sizeTransformer.setMinSize(30f);
        sizeTransformer.setMaxSize(150f);
        controller.transform(degreeRanking, sizeTransformer);

        PreviewModel previewModel = Lookup.getDefault().lookup(PreviewController.class).getModel();
        previewModel.getProperties().putValue(PreviewProperty.NODE_BORDER_WIDTH, 5);
        previewModel.getProperties().putValue(PreviewProperty.EDGE_OPACITY, 30);
        previewModel.getProperties().putValue(PreviewProperty.EDGE_THICKNESS, 0.2f);
    }

    private void exportFile(String output) {
        ExportController controller = Lookup.getDefault().lookup(ExportController.class);
        PNGExporter exporter = (PNGExporter) controller.getExporter("png");
        exporter.setTransparentBackground(true);
        exporter.setWidth(1920);
        exporter.setHeight(1080);
        try {
            controller.exportFile(new File("output/" + output), exporter);
        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private void doLayout() {
        GraphModel model = Lookup.getDefault().lookup(GraphController.class).getModel();
        GeoLayout layout = (GeoLayout) new GeoLayoutBuilder().buildLayout();
        layout.setGraphModel(model);
        layout.initAlgo();
        while (layout.canAlgo()) {
            layout.goAlgo();
        }
    }

    private void importFileToWorkspace(String input, Workspace workspace) {
        ImportController controller = Lookup.getDefault().lookup(ImportController.class);
        Container container = null;
        try {
            File file = new File(input);
            container = controller.importFile(file);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        controller.process(container, new DefaultProcessor(), workspace);
    }

    public static void main(String[] args) {
        File[] files = new File("gexf_files/").listFiles(f -> f.getName().endsWith("gexf"));
        for (File file : files) {
            String name = file.getName().substring(0, 8) + ".png";
            System.out.println(name);
            Main program = new Main();
            program.createGraph(file.getPath(), name);
        }
    }
}