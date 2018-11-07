package com.sankuai.msgp.common.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yves on 17/1/3.
 */
public class Pdl {

    private String owt;
    private String pdl;
    private List<User> owners = new ArrayList<User>();

    public Pdl(String owt) {
        this.owt = owt;
    }
    public Pdl(String owt, String pdl) {
        this.owt = owt;
        this.pdl = pdl;
    }

    public String getOwt() {
        return owt;
    }

    public void setOwt(String owt) {
        this.owt = owt;
    }

    public String getPdl() {
        return pdl;
    }

    public void setPdl(String pdl) {
        this.pdl = pdl;
    }

    public List<User> getOwners() {
        return owners;
    }

    public void setOwners(List<User> owners) {
        this.owners = owners;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).
                append(owt).
                append(pdl).
                toHashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (o == this)
            return true;
        if (o.getClass() != getClass())
            return false;
        Pdl e = (Pdl) o;
        return new EqualsBuilder().
                append(getOwt(), e.getOwt()).
                append(getPdl(), e.getPdl()).
                isEquals();
    }
    public String toString(){
        return "Pdl{" +
                "owt=" + owt +
                ", pdl=" + pdl +
                '}';
    }
}
