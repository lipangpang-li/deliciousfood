package org.example.entity;

import lombok.Getter;
import lombok.Setter;
import org.example.entity.basic.BasicEntity;

@Getter
@Setter
public class FoodStep extends BasicEntity {

    private String id;

    private String name;

    private Integer number;

    private String detil;

    private String ppictureId;

    private String videoId;

    private String deliciousFoodId;
}
