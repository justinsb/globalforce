package us.globalforce.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "users")
public class HumanWorker {
    @Id
    public int id;

}
