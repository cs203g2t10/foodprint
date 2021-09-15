package foodprint.backend.model;


import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table

public class Ingredient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ingredientId")
    private Integer ingredientId;

    @Column(name = "ingredientName")
    @Schema(defaultValue = "Salmon")
    private String ingredientName;

    @Column(name = "ingredientDesc")
    @Schema(defaultValue = "Sashimi grade salmon")
    private String ingredientDesc;

    @Column(name = "picturesPath")
    private String picturesPath;

    // @Column(name = "ingredientPrice")
    // private Double ingredientPrice;

    @Column(name = "units")
    @Schema(defaultValue = "1")
    private String units;

    @ManyToMany(mappedBy = "ingredients")
    private Set<Food> food = new HashSet<>();

    protected Ingredient () { }

    public Ingredient (String ingredientName) {
        this.ingredientName = ingredientName;
        // this.ingredientPrice = ingredientPrice;
    }

    // Mutators and Accessors
    public Integer getIngredientId() {
        return this.ingredientId;
    }

    public String getIngredientName() {
        return this.ingredientName;
    }

    public void setIngredientName(String ingredientName) {
        this.ingredientName = ingredientName;
    }

    public String getIngredientDesc() {
        return this.ingredientDesc;
    }

    public void setIngredientDesc(String ingredientDesc) {
        this.ingredientDesc = ingredientDesc;
    }

    // public Double getIngredientPrice() {
    //     return this.ingredientPrice;
    // }

    // public void setIngredientPrice(Double ingredientPrice) {
    //     this.ingredientPrice = ingredientPrice;
    // }

    public String getUnits() {
        return this.units;
    }

    public void setUnits (String units) {
        this.units = units;
    }

    public List<String> getPicturesPath() {
        if (picturesPath == null) {
            return null;
        }

        String[] arr = picturesPath.split(",");
        List<String> list = Arrays.asList(arr);
        return list;
    }

    public void setPicturesPath(List<String> list) {
        if (!list.isEmpty()) {
            String picturesPath = String.join(",", list);
            this.picturesPath = picturesPath;
        }
    }

}