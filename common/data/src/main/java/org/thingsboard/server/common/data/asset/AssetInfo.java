package org.thingsboard.server.common.data.asset;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.thingsboard.server.common.data.id.AssetId;

@ApiModel
@Data
public class AssetInfo extends Asset {

    @ApiModelProperty(position = 9, value = "Title of the Customer that owns the asset.", accessMode = ApiModelProperty.AccessMode.READ_ONLY)
    private String customerTitle;
    @ApiModelProperty(position = 10, value = "Indicates special 'Public' Customer that is auto-generated to use the assets on public dashboards.", accessMode = ApiModelProperty.AccessMode.READ_ONLY)
    private boolean customerIsPublic;

    public AssetInfo() {
        super();
    }

    public AssetInfo(AssetId assetId) {
        super(assetId);
    }

    public AssetInfo(Asset asset, String customerTitle, boolean customerIsPublic) {
        super(asset);
        this.customerTitle = customerTitle;
        this.customerIsPublic = customerIsPublic;
    }
}
