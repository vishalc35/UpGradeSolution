package com.vishal.campsite.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import java.util.List;
import java.util.Set;

public class Campsite {

    private static final long serialVersionUID = -8784956086870813508L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private String name;

    @Transient
    private Set<Long> availableDays;

    // 1 campsite => N reserves
    @OneToMany(mappedBy="campsite", fetch= FetchType.LAZY, cascade= CascadeType.ALL)
    private List<Reserve> reserves;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Reserve> getReserves() {
        return reserves;
    }

    public void setReserves(List<Reserve> reserves) {
        this.reserves = reserves;
    }

    public Set<Long> getAvailableDays() {
        return availableDays;
    }

    public void setAvailableDays(Set<Long> availableDays) {
        this.availableDays = availableDays;
    }

    @Override
    public String toString() {
        return "Campsite{" +
                "name='" + name + '\'' +
                ", availableDays=" + availableDays +
                ", reserves=" + reserves +
                '}';
    }
}
