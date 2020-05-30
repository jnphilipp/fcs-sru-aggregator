package eu.clarin.sru.fcs.aggregator.rest;

import java.io.Serializable;

/*import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;*/

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class RegistryUser implements Serializable {
    private String name;
    private String principalName;

    private Long id;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setPrincipalName(String principalName) {
        this.principalName = principalName;
    }

    public String getPrincipalName() {
        return principalName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        if (getName() == null) {
            return getPrincipalName();
        } else {
            return String.format("%s [%s]", getName(), getPrincipalName());
        }
    }

}
