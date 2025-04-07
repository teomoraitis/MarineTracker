keytool -exportcert \
  -alias selfsigned-cert \
  -keystore keystore.p12 \
  -file selfsigned-cert.crt \
  -rfc \
  -storepass keystore_password