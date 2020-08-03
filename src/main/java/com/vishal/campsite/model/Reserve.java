package com.vishal.campsite.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class Reserve {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Getter @Setter
    private Long id;

    @Column
    @Getter @Setter
    private String email;

    @Column
    @Getter @Setter
    private String fullName;

    @Column
    @Getter @Setter
    private Long arrivalDate;

    @Column
    @Getter @Setter
    private Long departureDate;

    // N reserves => 1 campsite
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campsite_id")
    @JsonIgnore
    @Getter @Setter
    private Campsite campsite;

    public Reserve() {
    }

    public Reserve(String email, String fullName, Long arrivalDate, Long departureDate) {
        this.email = email;
        this.fullName = fullName;
        this.arrivalDate = arrivalDate;
        this.departureDate = departureDate;
    }
}
