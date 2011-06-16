package org.dcache.gplazma;

import static com.google.common.base.Preconditions.checkNotNull;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.security.auth.Subject;

import org.dcache.gplazma.configuration.ConfigurationItem;
import org.dcache.gplazma.configuration.ConfigurationItemControl;
import org.dcache.gplazma.configuration.ConfigurationItemType;
import org.dcache.gplazma.configuration.ConfigurationLoadingStrategy;
import org.dcache.gplazma.loader.CachingPluginLoaderDecorator;
import org.dcache.gplazma.loader.PluginLoader;
import org.dcache.gplazma.loader.XmlResourcePluginLoader;
import org.dcache.gplazma.plugins.GPlazmaAccountPlugin;
import org.dcache.gplazma.plugins.GPlazmaAuthenticationPlugin;
import org.dcache.gplazma.plugins.GPlazmaIdentityPlugin;
import org.dcache.gplazma.plugins.GPlazmaMappingPlugin;
import org.dcache.gplazma.plugins.GPlazmaPlugin;
import org.dcache.gplazma.plugins.GPlazmaSessionPlugin;
import org.dcache.gplazma.strategies.AccountStrategy;
import org.dcache.gplazma.strategies.AuthenticationStrategy;
import org.dcache.gplazma.strategies.GPlazmaPluginElement;
import org.dcache.gplazma.strategies.IdentityStrategy;
import org.dcache.gplazma.strategies.MappingStrategy;
import org.dcache.gplazma.strategies.SessionStrategy;
import org.dcache.gplazma.strategies.StrategyFactory;
import org.dcache.gplazma.validation.ValidationStrategy;
import org.dcache.gplazma.validation.ValidationStrategyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GPlazma {

    private static final Logger LOGGER =
        LoggerFactory.getLogger( GPlazma.class);

    private Properties _globalProperties;
    private boolean _globalPropertiesHaveUpdated = false;

    private PluginLoader pluginLoader;

    private List<GPlazmaPluginElement<GPlazmaAuthenticationPlugin>>
            authenticationPluginElements;
    private List<GPlazmaPluginElement<GPlazmaMappingPlugin>>
            mappingPluginElements;
    private List<GPlazmaPluginElement<GPlazmaAccountPlugin>>
            accountPluginElements;
    private List<GPlazmaPluginElement<GPlazmaSessionPlugin>>
            sessionPluginElements;
    private List<GPlazmaPluginElement<GPlazmaIdentityPlugin>>
            identityPluginElements;

    private final ConfigurationLoadingStrategy configurationLoadingStrategy;
    private AuthenticationStrategy authenticationStrategy;
    private MappingStrategy mappingStrategy;
    private AccountStrategy accountStrategy;
    private SessionStrategy sessionStrategy;
    private ValidationStrategy validationStrategy;
    private IdentityStrategy identityStrategy;

    /**
     * @param configurationLoadingStrategy The strategy for loading the plugin configuration.
     * @param environment Map holding environment variables with their values. Must not be null.
     */
    public GPlazma(ConfigurationLoadingStrategy configurationLoadingStrategy, Map<String, Object> environment) {
        this.configurationLoadingStrategy = configurationLoadingStrategy;
        _globalProperties = environmentToProperties(environment);
        loadPlugins();
        initStrategies();
    }

    /**
     * @param environment Map holding environment variables with their values. Must not be null.
     */
    public void updateGlobalProperties(Map<String, Object> environment) {
        synchronized (configurationLoadingStrategy) {
            _globalProperties = environmentToProperties(environment);
            _globalPropertiesHaveUpdated = true;
        }
    }

    public LoginReply login(Subject subject) throws AuthenticationException {
        checkNotNull(subject, "subject is null");

        AuthenticationStrategy currentAuthenticationStrategy;
        MappingStrategy currentMappingStrategy;
        AccountStrategy currentAccountStrategy;
        SessionStrategy currentSessionStrategy;

        synchronized (configurationLoadingStrategy) {
            updatePluginConfig();

            currentAuthenticationStrategy = this.authenticationStrategy;
            currentMappingStrategy = this.mappingStrategy;
            currentAccountStrategy = this.accountStrategy;
            currentSessionStrategy = this.sessionStrategy;
         }

         SessionID sessionId = null;
         Set<Principal> identifiedPrincipals = new HashSet<Principal>();
         identifiedPrincipals.addAll(subject.getPrincipals());
         Set<Principal>  authorizedPrincipals = new HashSet<Principal>();
         Set<Object> attributes = new HashSet<Object>();
         currentAuthenticationStrategy.authenticate(
                 sessionId,
                 subject.getPublicCredentials(),
                 subject.getPrivateCredentials(),
                 identifiedPrincipals);
         currentMappingStrategy.map(
                 sessionId,
                 identifiedPrincipals,
                 authorizedPrincipals);
         currentAccountStrategy.account(
                 sessionId,
                 authorizedPrincipals);
         currentSessionStrategy.session(
                 sessionId,
                 authorizedPrincipals,
                 attributes);
         LoginReply reply = new LoginReply();
         Subject replySubject = new Subject(
                 false,
                 authorizedPrincipals,
                 subject.getPublicCredentials(),
                 subject.getPrivateCredentials());
         reply.setSubject(replySubject);
         reply.setSessionAttributes(attributes);

         validationStrategy.validate(sessionId, reply);

         return reply;
    }

    public Principal map(Principal principal) throws AuthenticationException {
        return getIdentityStrategy().map(principal);
    }

    public Set<Principal> reverseMap(Principal principal) throws AuthenticationException {
        return getIdentityStrategy().reverseMap(principal);
    }

    private IdentityStrategy getIdentityStrategy() {
        synchronized (configurationLoadingStrategy) {
            updatePluginConfig();

            return this.identityStrategy;
        }
    }

    private void loadPlugins() {

        pluginLoader = new CachingPluginLoaderDecorator(
                XmlResourcePluginLoader.newPluginLoader());
        pluginLoader.init();

        authenticationPluginElements = new ArrayList<GPlazmaPluginElement<GPlazmaAuthenticationPlugin>>();
        mappingPluginElements = new ArrayList<GPlazmaPluginElement<GPlazmaMappingPlugin>>();
        accountPluginElements = new ArrayList<GPlazmaPluginElement<GPlazmaAccountPlugin>>();
        sessionPluginElements = new ArrayList<GPlazmaPluginElement<GPlazmaSessionPlugin>>();
        identityPluginElements = new ArrayList<GPlazmaPluginElement<GPlazmaIdentityPlugin>>();

        for(ConfigurationItem configItem:
                configurationLoadingStrategy.
                        load().getConfigurationItemList()) {
            String pluginName = configItem.getPluginName();

            Properties pluginProperties = configItem.getPluginConfiguration();
            Properties combinedProperties = new Properties(_globalProperties);
            combinedProperties.putAll(pluginProperties);

            ConfigurationItemType type = configItem.getType();
            ConfigurationItemControl control = configItem.getControl();
            GPlazmaPlugin plugin = pluginLoader.newPluginByName(pluginName, combinedProperties);
            classifyPlugin(type, plugin, pluginName, control);
        }
    }

    private void initStrategies() {
        StrategyFactory factory = StrategyFactory.getInstance();
        authenticationStrategy = factory.newAuthenticationStrategy();
        authenticationStrategy.setPlugins(authenticationPluginElements);
        mappingStrategy = factory.newMappingStrategy();
        mappingStrategy.setPlugins(mappingPluginElements);
        accountStrategy = factory.newAccountStrategy();
        accountStrategy.setPlugins(accountPluginElements);
        sessionStrategy = factory.newSessionStrategy();
        sessionStrategy.setPlugins(sessionPluginElements);
        identityStrategy = factory.newIdentityStrategy();
        identityStrategy.setPlugins(identityPluginElements);

        ValidationStrategyFactory validationFactory =
                ValidationStrategyFactory.getInstance();
        validationStrategy = validationFactory.newValidationStrategy();
    }

    private void updatePluginConfig() {
        if (_globalPropertiesHaveUpdated || configurationLoadingStrategy.hasUpdated()) {
            LOGGER.debug("configuration has been updated, reloading plugins");
            loadPlugins();
            initStrategies();
            _globalPropertiesHaveUpdated = false;
        }
    }

    private void classifyPlugin(
            ConfigurationItemType type,
            GPlazmaPlugin plugin,
            String pluginName,
            ConfigurationItemControl control) throws IllegalArgumentException {
        if(!type.getType().isAssignableFrom(plugin.getClass())) {
                    throw new IllegalArgumentException(" plugin " + pluginName +
                            " (java class  " +
                            plugin.getClass().getCanonicalName() +
                            ") does not support being loaded as type " + type );
        }
        switch (type) {
            case AUTHENTICATION:
            {
                storePluginElement(plugin, control, authenticationPluginElements);
                break;
            }
            case MAPPING:
            {
                storePluginElement(plugin, control, mappingPluginElements);
                break;
            }
            case ACCOUNT:
            {
                storePluginElement(plugin, control, accountPluginElements);
                break;
            }
            case SESSION:
            {
                storePluginElement(plugin, control, sessionPluginElements);
                break;
            }
            case IDENTITY: {
                storePluginElement(plugin, control, identityPluginElements);
                break;
            }
            default:
            {
                throw new IllegalArgumentException("Unknown type " + type);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends GPlazmaPlugin> void storePluginElement (
            GPlazmaPlugin plugin,
            ConfigurationItemControl control,
            List<GPlazmaPluginElement<T>> pluginElements)
            throws IllegalArgumentException {
        // we are forced to use unchecked cast here, as the generics do not support
        // instanceof, but we have checked the type before calling storePluginElement
        T authPlugin = (T) plugin;
        GPlazmaPluginElement<T> pluginElement = new GPlazmaPluginElement<T>(authPlugin, control);
        pluginElements.add(pluginElement);
    }

    private Properties environmentToProperties(Map<String, Object> environment) {

        Properties properties = new Properties();
        for (Map.Entry<String, Object> entry : environment.entrySet()) {
            properties.put(entry.getKey(), entry.getValue().toString());
        }

        return properties;
    }
}