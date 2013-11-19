package us.globalforce.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "human_worker")
@XmlRootElement
public class HumanWorker {
    @Id
    public int id;

}
