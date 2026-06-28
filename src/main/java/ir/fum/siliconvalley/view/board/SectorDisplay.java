package ir.fum.siliconvalley.view.board;

import ir.fum.siliconvalley.model.enums.SectorType;
import javafx.scene.layout.Pane;
import javafx.scene.shape.SVGPath;
import java.util.Map;

/** Visual metadata for rendering sectors on the board. */
final class SectorDisplay {
    private static final Map<SectorType, String> SECTOR_NAMES = Map.of(
            SectorType.AI_HUB, "AI  SECTOR",
            SectorType.IP_QUARTER, "PATENTS  SECTOR",
            SectorType.CLOUD_CAMPUS, "CLOUD  SECTOR",
            SectorType.DATA_VALLEY, "DATA  SECTOR",
            SectorType.FINTECH_DISTRICT, "FINTECH  SECTOR",
            SectorType.REGULATORY_ZONE, "REGULATORY  ZONE"
    );

    private static final Map<SectorType, String> ICONS = Map.of(
            SectorType.AI_HUB, SvgIcons.AI,
            SectorType.IP_QUARTER, SvgIcons.PATENT,
            SectorType.CLOUD_CAMPUS, SvgIcons.CLOUD,
            SectorType.DATA_VALLEY, SvgIcons.DATA,
            SectorType.FINTECH_DISTRICT, SvgIcons.FINTECH,
            SectorType.REGULATORY_ZONE, SvgIcons.REGULATORY
    );

    private static final Map<SectorType, String> CATEGORY_LABELS = Map.of(
            SectorType.AI_HUB, "AI Hub",
            SectorType.IP_QUARTER, "IP Quarter",
            SectorType.CLOUD_CAMPUS, "Cloud Campus",
            SectorType.DATA_VALLEY, "Data Valley",
            SectorType.FINTECH_DISTRICT, "Fintech District",
            SectorType.REGULATORY_ZONE, "Regulatory Zone"
    );

    private static final Map<SectorType, String> STYLE_CLASSES = Map.of(
            SectorType.AI_HUB, "sector-ai",
            SectorType.IP_QUARTER, "sector-ip",
            SectorType.CLOUD_CAMPUS, "sector-cloud",
            SectorType.DATA_VALLEY, "sector-data",
            SectorType.FINTECH_DISTRICT, "sector-fintech",
            SectorType.REGULATORY_ZONE, "sector-regulatory"
    );

    private SectorDisplay() {
    }

    public static String sectorName(SectorType type, int row) {
        return SECTOR_NAMES.get(type);
    }

    public static Pane icon(SectorType type) {
        return createIcon(ICONS.get(type), 25);
    }

    private static Pane createIcon(String path, double size) {
        SVGPath svg = new SVGPath();
        svg.setContent(path);
        svg.setFill(javafx.scene.paint.Color.WHITE);

        double sourceSize = 256.0;
        double scale = size / sourceSize;

        svg.setScaleX(scale);
        svg.setScaleY(scale);
        svg.setLayoutX(-(sourceSize - size) / 2.0);
        svg.setLayoutY(-(sourceSize - size) / 2.0);

        Pane wrapper = new Pane(svg);
        wrapper.setMaxSize(size, size);
        wrapper.setMinSize(size, size);
        wrapper.setPrefSize(size, size);

        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(size, size);
        wrapper.setClip(clip);

        return wrapper;
    }

    public static String categoryLabel(SectorType type) {
        return CATEGORY_LABELS.get(type);
    }

    public static String styleClass(SectorType type) {
        return STYLE_CLASSES.get(type);
    }
}
