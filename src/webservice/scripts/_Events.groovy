eventConfigureTomcat = { tomcat ->
    tomcat.addUser("odisee", "odisee")
    tomcat.addRole("odisee", "odisee")
}
