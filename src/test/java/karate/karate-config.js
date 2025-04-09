function fn() {
  var env = karate.env; // get system property 'karate.env'
  karate.log('karate.env system property was:', env);
  if (!env) {
    env = 'dev';
  }
  var config = {
    env: env,
    ftocPath: karate.properties['ftoc.path'] || '../../../target/ftoc-*-jar-with-dependencies.jar',
    featuresPath: 'src/test/resources/ftoc/test-feature-files',
    isWindows: karate.match(java.lang.System.getProperty('os.name').toLowerCase(), '*win*').pass
  };
  
  // Configure command prefix based on OS
  config.cmdPrefix = config.isWindows ? 'cmd /c ' : '';
  
  // Configure full ftoc command
  config.ftocCommand = 'java -jar ' + config.ftocPath;
  
  return config;
}