(ns biis-integration.config.config)

(def paysafe-config {:fbd         {:account-number ""
                                   :username       ""
                                   :password       ""}
                     :rsa         {:account-number ""
                                   :username       ""
                                   :password       ""}
                     :card-number ""})

(def applied-config {:url      "https://api-dev.apigee.appliedcloudservices.co.uk/applied-api-connect/v1"
                     :username ""
                     :password ""})

(def wallet-config {:url             "https://api.ecommerce.ci.bluegrizzly-biis.io"
                    :cognitoClientId ""
                    :username        ""
                    :password        ""})