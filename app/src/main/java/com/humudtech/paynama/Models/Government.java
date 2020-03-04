package com.humudtech.paynama.Models;

public class Government {
    private String CompleteName;
    private String Code;

    public Government(String CompleteName, String Code) {
        this.CompleteName = CompleteName;
        this.Code = Code;
    }


    public String getCompleteName() {
        return CompleteName;
    }

    public void setId(String CompleteName) {
        this.CompleteName = CompleteName;
    }

    public String getCode() {
        return Code;
    }

    public void setName(String Code) {
        this.Code = Code;
    }


    //to display object as a string in spinner
    @Override
    public String toString() {
        return CompleteName;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Government){
            Government g = (Government )obj;
            if(g.getCode().equals(Code) && g.getCompleteName()==CompleteName ) return true;
        }

        return false;
    }
}
