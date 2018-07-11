/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.example.instrumentation;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;

import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.identity.IdentityProviderException;
import org.camunda.bpm.engine.impl.interceptor.Session;
import org.camunda.bpm.identity.impl.ldap.LdapAuthenticationException;
import org.camunda.bpm.identity.impl.ldap.LdapConfiguration;
import org.camunda.bpm.identity.impl.ldap.LdapGroupQuery;
import org.camunda.bpm.identity.impl.ldap.LdapIdentityProviderFactory;
import org.camunda.bpm.identity.impl.ldap.LdapIdentityProviderSession;
import org.camunda.bpm.identity.impl.ldap.plugin.LdapIdentityProviderPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Thorben Lindhauer
 *
 */
public class InstrumentedLdapPlugin extends LdapIdentityProviderPlugin {

  private static final Logger LOGGER = LoggerFactory.getLogger("org.camunda.example.instrumentation");

  @Override
  public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
    super.preInit(processEngineConfiguration);

    SessionFactory sessionFactory = new SessionFactory();
    sessionFactory.setLdapConfiguration(this);
    processEngineConfiguration.setIdentityProviderSessionFactory(sessionFactory);

  }


  private static class SessionFactory extends LdapIdentityProviderFactory
  {
    @Override
    public Session openSession() {
      return new InstrumentedLdapIdentityProviderSession(ldapConfiguration);
    }
  }

  private static class InstrumentedLdapIdentityProviderSession extends LdapIdentityProviderSession {

    public InstrumentedLdapIdentityProviderSession(LdapConfiguration ldapConfiguration) {
      super(ldapConfiguration);
    }


    @Override
    public List<Group> findGroupByQueryCriteria(LdapGroupQuery query) {

      StringBuilder sb = new StringBuilder();
      sb.append("Querying for groups of user: ");
      sb.append(query.getUserId());
      LOGGER.debug(sb.toString());

      List<Group> groups = super.findGroupByQueryCriteria(query);

      sb = new StringBuilder();
      sb.append("Fetched groups: ");
      sb.append(groups);
      LOGGER.debug(sb.toString());

      return groups;
    }

    @Override
    protected InitialLdapContext openContext(String userDn, String password) {
      Hashtable<String, Object> env = new Hashtable<String, Object>();
      env.put(Context.INITIAL_CONTEXT_FACTORY, ldapConfiguration.getInitialContextFactory());
      env.put(Context.SECURITY_AUTHENTICATION, ldapConfiguration.getSecurityAuthentication());
      env.put(Context.PROVIDER_URL, ldapConfiguration.getServerUrl());
      env.put(Context.SECURITY_PRINCIPAL, userDn);
      env.put(Context.SECURITY_CREDENTIALS, password);

      // for anonymous login
      if(ldapConfiguration.isAllowAnonymousLogin() && password.isEmpty()) {
        env.put(Context.SECURITY_AUTHENTICATION, "none");
      }

      if(ldapConfiguration.isUseSsl()) {
        env.put(Context.SECURITY_PROTOCOL, "ssl");
      }

      // add additional properties
      Map<String, String> contextProperties = ldapConfiguration.getContextProperties();
      if(contextProperties != null) {
        env.putAll(contextProperties);
      }

      try {
        return new InstrumentedLdapContext(env, null);

      } catch(AuthenticationException e) {
        throw new LdapAuthenticationException("Could not authenticate with LDAP server", e);

      } catch(NamingException e) {
        throw new IdentityProviderException("Could not connect to LDAP server", e);

      }
    }
  }

  private static class InstrumentedLdapContext extends InitialLdapContext
  {

    public InstrumentedLdapContext(Hashtable<?, ?> environment, Control[] connCtls) throws NamingException {
      super(environment, connCtls);
    }

    @Override
    public NamingEnumeration<SearchResult> search(String name, String filter, SearchControls cons) throws NamingException {
      StringBuilder sb = new StringBuilder();
      sb.append("Querying LDAP for name: ");
      sb.append(name);
      sb.append(", filter: ");
      sb.append(filter);
      LOGGER.debug(sb.toString());

      return super.search(name, filter, cons);
    }

  }
}
