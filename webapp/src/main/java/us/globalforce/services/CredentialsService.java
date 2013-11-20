package us.globalforce.services;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.globalforce.model.Credential;

@Singleton
public class CredentialsService {

    private static final Logger log = LoggerFactory.getLogger(CredentialsService.class);

    @Inject
    JdbcRepository repository;

    @Inject
    SalesforceHooks salesforceHooks;

    public void insertCredential(Credential credential) throws IOException {
        repository.insertCredential(credential);

        salesforceHooks.ensureHooked(credential);
    }

    public void start() {
        for (String organization : repository.findAllCredentialOrganizations()) {
            try {
                Credential credential = repository.findCredential(organization);
                salesforceHooks.ensureHooked(credential);
            } catch (Exception e) {
                log.warn("Unable to hook organization: " + organization, e);
            }
        }
    }
}
