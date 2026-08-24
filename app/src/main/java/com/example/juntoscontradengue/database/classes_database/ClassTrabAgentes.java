    package com.example.juntoscontradengue.database.classes_database;
    
    public class    ClassTrabAgentes {
        private String id;          // ID gerado (key do Firebase)
        private String urlMidia;         // URL da imagem ou vídeo no Storage
        private String titulo;      // Nome para exibir
        private String tipo;        // "imagem" ou "video"
        private long dataUpload;    // Timestamp do upload (System.currentTimeMillis())
        public ClassTrabAgentes() { }
    
        public ClassTrabAgentes(String id, String titulo, String urlMidia, String tipo, long dataUpload) {
            this.id = id;
            this.titulo = titulo;
            this.urlMidia = urlMidia;
            this.tipo = tipo;
            this.dataUpload = dataUpload;
        }
    
        // getters e setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
    
        public String getTitulo() { return titulo; }
        public void setTitulo(String titulo) { this.titulo = titulo; }
    
        public String getUrlMidia() { return urlMidia; }
        public void setUrlMidia(String urlMidia) { this.urlMidia = urlMidia; }
    
        public String getTipo() { return tipo; }
        public void setTipo(String tipo) { this.tipo = tipo; }
    
        public long getDataUpload() { return dataUpload; }
        public void setDataUpload(long dataUpload) { this.dataUpload = dataUpload; }
    }
    
