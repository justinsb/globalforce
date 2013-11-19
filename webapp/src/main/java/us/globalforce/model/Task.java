package us.globalforce.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "task")
public class Task {
    @Id
    public long id;

    @Column
    public String problem;

    @Column(name = "objectid")
    public String objectId;

    @Column
    public int sequence;

    @Column
    public String input;

    @Column
    public Integer worker;
    @Column
    public String decision;
}
