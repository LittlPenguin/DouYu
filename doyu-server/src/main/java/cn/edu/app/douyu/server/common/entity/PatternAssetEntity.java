package cn.edu.app.douyu.server.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "pattern_assets")
public class PatternAssetEntity {
    @Id @Column(name = "id", length = 64) private String id;
    @Column(name = "job_id", length = 64, nullable = false) private String jobId;
    @Column(name = "owner_id", length = 64, nullable = false) private String ownerId;
    @Column(name = "preview_file_id", length = 64, nullable = false) private String previewFileId;
    @Column(name = "grid_file_id", length = 64, nullable = false) private String gridFileId;
    @Column(name = "color_map_file_id", length = 64, nullable = false) private String colorMapFileId;
    @Column(name = "pdf_file_id", length = 64) private String pdfFileId;
    @Column(name = "bead_size", length = 32, nullable = false) private String beadSize;
    @Column(name = "width_cells", nullable = false) private int widthCells;
    @Column(name = "height_cells", nullable = false) private int heightCells;
    @Column(name = "total_beads", nullable = false) private int totalBeads;
    @Column(name = "materials_json", nullable = false, columnDefinition = "text") private String materialsJson;
    @Column(name = "status", length = 32, nullable = false) private String status;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    public PatternAssetEntity() {}

    public String getId() { return id; } public void setId(String id) { this.id = id; }
    public String getJobId() { return jobId; } public void setJobId(String jobId) { this.jobId = jobId; }
    public String getOwnerId() { return ownerId; } public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    public String getPreviewFileId() { return previewFileId; } public void setPreviewFileId(String previewFileId) { this.previewFileId = previewFileId; }
    public String getGridFileId() { return gridFileId; } public void setGridFileId(String gridFileId) { this.gridFileId = gridFileId; }
    public String getColorMapFileId() { return colorMapFileId; } public void setColorMapFileId(String colorMapFileId) { this.colorMapFileId = colorMapFileId; }
    public String getPdfFileId() { return pdfFileId; } public void setPdfFileId(String pdfFileId) { this.pdfFileId = pdfFileId; }
    public String getBeadSize() { return beadSize; } public void setBeadSize(String beadSize) { this.beadSize = beadSize; }
    public int getWidthCells() { return widthCells; } public void setWidthCells(int widthCells) { this.widthCells = widthCells; }
    public int getHeightCells() { return heightCells; } public void setHeightCells(int heightCells) { this.heightCells = heightCells; }
    public int getTotalBeads() { return totalBeads; } public void setTotalBeads(int totalBeads) { this.totalBeads = totalBeads; }
    public String getMaterialsJson() { return materialsJson; } public void setMaterialsJson(String materialsJson) { this.materialsJson = materialsJson; }
    public String getStatus() { return status; } public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; } public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; } public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
