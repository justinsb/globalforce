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

    @Column(name = "refreshtoken")
    public String refreshToken;

    @Column(name = "created")
    public long createdAt;
}
