package cn.edu.app.douyu.model;

public class CreatePatternJobRequest {
    public final String inputFileId;
    public final String beadSize;
    public final String targetSize;
    public final String difficulty;
    public final String paletteId;
    public final String style;

    public CreatePatternJobRequest(String inputFileId, String beadSize, String targetSize, String difficulty, String paletteId, String style) {
        this.inputFileId = inputFileId;
        this.beadSize = beadSize;
        this.targetSize = targetSize;
        this.difficulty = difficulty;
        this.paletteId = paletteId;
        this.style = style;
    }
}
