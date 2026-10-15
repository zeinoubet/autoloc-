package tn.esprit.elabed_zeineb_4cce11.service;

import tn.esprit.elabed_zeineb_4cce11.domain.Client;

import java.util.List;

public interface IClientService {

    Client create(Client client);

    Client findById(Long id);

    List<Client> findAll();

    Client update(Long id, Client client);

    void deleteById(Long id);
}
