package us.globalforce.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "credential")
public class Credential {
    @Id
    public long id;

    @Column
    public String organization;

    @Column(name = "userid")
    public String userId;

    @Column(name = "objectid")
    public String objectId;

    @Column
    public int sequence;

    @Column
    public String input;

    @Column
    public String worker;

    @Column
    public String decision;
}
