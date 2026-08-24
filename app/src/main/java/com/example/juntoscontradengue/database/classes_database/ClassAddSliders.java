package com.example.juntoscontradengue.database.classes_database;

public class ClassAddSliders {
    private Long id;
    private String image_1;
    private String image_2;
    private String image_3;
    private String image_4;
    private String image_5;
    private String url_imagem;

    public ClassAddSliders() {
    }

    public ClassAddSliders(Long id, String image_1, String image_2, String image_3, String image_4, String image_5, String url_imagem) {
        this.id = id;
        this.image_1 = image_1;
        this.image_2 = image_2;
        this.image_3 = image_3;
        this.image_4 = image_4;
        this.image_5 = image_5;
        this.url_imagem = url_imagem;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getImage_1() {
        return image_1;
    }

    public void setImage_1(String image_1) {
        this.image_1 = image_1;
    }

    public String getImage_2() {
        return image_2;
    }

    public void setImage_2(String image_2) {
        this.image_2 = image_2;
    }

    public String getImage_3() {
        return image_3;
    }

    public void setImage_3(String image_3) {
        this.image_3 = image_3;
    }

    public String getImage_4() {
        return image_4;
    }

    public void setImage_4(String image_4) {
        this.image_4 = image_4;
    }

    public String getImage_5() {
        return image_5;
    }

    public void setImage_5(String image_5) {
        this.image_5 = image_5;
    }

    public String getUrl_imagem() {
        return url_imagem;
    }

    public void setUrl_imagem(String url_imagem) {
        this.url_imagem = url_imagem;
    }
}