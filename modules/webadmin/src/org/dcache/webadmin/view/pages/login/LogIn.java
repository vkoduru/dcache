package org.dcache.webadmin.view.pages.login;

import java.security.cert.X509Certificate;
import javax.servlet.http.HttpServletRequest;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.protocol.https.RequireHttps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.dcache.webadmin.controller.LogInService;
import org.dcache.webadmin.controller.exceptions.LogInServiceException;
import org.dcache.webadmin.view.beans.LogInBean;
import org.dcache.webadmin.view.beans.UserBean;
import org.dcache.webadmin.view.beans.WebAdminInterfaceSession;
import org.dcache.webadmin.view.pages.basepage.BasePage;
import org.dcache.webadmin.view.util.DefaultFocusBehaviour;

@RequireHttps
public class LogIn extends BasePage {

    public static final String X509_CERTIFICATE_ATTRIBUTE =
            "javax.servlet.request.X509Certificate";
    private static final Logger _log = LoggerFactory.getLogger(LogIn.class);

    public LogIn() {
        final FeedbackPanel feedback = new FeedbackPanel("feedback");
        add(new Label("dCacheInstanceName",
                getWebadminApplication().getDcacheName()));
        add(feedback);
        add(new LogInForm("LogInForm"));
    }

    private LogInService getLogInService() {
        return getWebadminApplication().getLogInService();
    }

    private class LogInForm extends StatelessForm {

        private TextField _username;
        private PasswordTextField _password;
        private CheckBox _rememberMe;
        private WebMarkupContainer _rememberMeRow;
        private LogInBean _logInModel;

        LogInForm(final String id) {
            super(id, new CompoundPropertyModel<LogInBean>(new LogInBean()));
            _logInModel = (LogInBean) getDefaultModelObject();

            _username = new TextField("username");
            _username.setRequired(true);
            add(_username);
            _password = new PasswordTextField("password");

            add(_password);
            add(new LogInButton("submit"));
            _rememberMeRow = new WebMarkupContainer("rememberMeRow");
            add(_rememberMeRow);
            _rememberMe = new CheckBox("remembering");
            _rememberMeRow.add(_rememberMe);
            Button certButton = new CertSignInButton("certsignin");
            certButton.add(new DefaultFocusBehaviour());
            add(certButton);
            setCookiePersistence(true);
        }

        @Override
        public final void onSubmit() {
            if (!_logInModel.isRemembering()) {
                forgetMe();
            }
        }

        /**
         * Removes persisted form data(cookie) for the login page
         */
        private final void forgetMe() {
            getPage().removePersistedFormData(LogInForm.class, true);
        }

        private void setCookiePersistence(boolean enable) {
            _username.setPersistent(enable);
            _rememberMe.setPersistent(enable);
        }

        private void setGoOnPage() {
            // If login has been called because the user was not yet
            // logged in, then continue to the original destination,
            // otherwise to the Home page
            if (!continueToOriginalDestination()) {
                setResponsePage(getApplication().getHomePage());
            }
        }

        private class LogInButton extends Button {

            public LogInButton(String id) {
                super(id);
            }

            @Override
            public void onSubmit() {
                WebAdminInterfaceSession session = getWebadminSession();
                try {
                    if (!session.isSignedIn()) {
                        _log.debug("username: {}", _logInModel.getUsername());
                        UserBean user = getLogInService().authenticate(
                                _logInModel.getUsername(),
                                _logInModel.getPassword().toCharArray());
                        session.setUser(user);
                    }
                    setGoOnPage();
                } catch (LogInServiceException ex) {
                    String cause = "unknown";
                    if (ex.getMessage() != null) {
                        cause = ex.getMessage();
                    }
//                  not a very good solution to take the cause and append it,
//                  because it is not localised this way - but dcache is english
//                  only anyway...
                    error(getStringResource("loginError") + " - cause: " + cause);
                    _log.debug("user/pwd sign in error - cause {}", cause);
                }
            }
        }

        private class CertSignInButton extends Button {

            public CertSignInButton(String id) {
                super(id);
//                deactivate checking of formdata for certsignin
                this.setDefaultFormProcessing(false);
            }

            @Override
            public void onSubmit() {
                WebAdminInterfaceSession session = getWebadminSession();
                try {
                    if (!session.isSignedIn()) {
                        X509Certificate[] certChain = getCertChain();
                        _log.debug("cert sign in");
                        UserBean user = getLogInService().authenticate(certChain);
                        session.setUser(user);
                    }
                    setGoOnPage();
                } catch (IllegalArgumentException ex) {
                    error(getStringResource("noCertError"));
                    _log.debug("no certificate provided");
                } catch (LogInServiceException ex) {
                    String cause = "unknown";
                    if (ex.getMessage() != null) {
                        cause = ex.getMessage();
                    }
                    error(getStringResource("loginError"));
                    _log.debug("cert sign in error - cause {}", cause);
                }
            }

            private X509Certificate[] getCertChain() {
                ServletWebRequest servletWebRequest = (ServletWebRequest) getRequest();
                HttpServletRequest request = servletWebRequest.getHttpServletRequest();
                Object certificate = request.getAttribute(X509_CERTIFICATE_ATTRIBUTE);
                X509Certificate[] chain = null;
                if (certificate instanceof X509Certificate[]) {
                    _log.debug("Certificate in request: {}", certificate.toString());
                    chain = (X509Certificate[]) certificate;
                } else {
                    throw new IllegalArgumentException();
                }
                return chain;
            }
        }
    }
}