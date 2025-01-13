package org.example.entity;

import lombok.Getter;
import lombok.Setter;
import org.example.entity.basic.BasicEntity;

@Setter
@Getter
public class DeliciousFood extends BasicEntity {

    private String id;

    private String name;

    //简介
    private String introduce;

    //步骤
    private String step;

    private String pictureId;

    private String videoId;

    private Integer likeCount;

    private Integer collectCount;

    //注意事项
    private String attention;

    //属性
    private String attribute;

    //材料
    private String ingredient;

    //食材文本
    private String ingredientText;

    //菜系
    private String caixi;

    //菜式
    private String caishi;
}
