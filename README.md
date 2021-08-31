# drugstore

## test

_unix_

```bash
./mvnw
```

_windows_

```cmd
mvnw
```

## run

_unix_

```bash
rm -rf ~/.m2/repository/sigma/software/leovegas/drugstore
./mvnw -DskipTests clean install -pl '!:accountancy-service,!:order-service,!:product-service,!:store-service'
./mvnw -DskipTests spring-boot:run -f accountancy/accountancy-service &
./mvnw -DskipTests spring-boot:run -f order/order-service &
./mvnw -DskipTests spring-boot:run -f product/product-service &
./mvnw -DskipTests spring-boot:run -f store/store-service &
```

_windows_

## run in docker

_unix_

```bash
./mvnw -DskipTests clean package
#./mvnw -f .dev -P down ; ./mvnw -f .dev -P up
./mvnw -f .dev -P up
./mvnw -f .dev -P logs
```

_windows_
mvnw -DskipTests clean package
mvnw -f .dev -P down && mvnw -f .dev -P up
mvnw -f .dev -P logs

TODO: low priority.

<!--

# Getting Started

### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.5.2/maven-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/2.5.2/maven-plugin/reference/html/#build-image)
* [Liquibase Migration](https://docs.spring.io/spring-boot/docs/2.5.2/reference/htmlsingle/#howto-execute-liquibase-database-migrations-on-startup)
* [Spring Data JPA](https://docs.spring.io/spring-boot/docs/2.5.2/reference/htmlsingle/#boot-features-jpa-and-spring-data)
* [Spring Web](https://docs.spring.io/spring-boot/docs/2.5.2/reference/htmlsingle/#boot-features-developing-web-applications)
* [Thymeleaf](https://docs.spring.io/spring-boot/docs/2.5.2/reference/htmlsingle/#boot-features-spring-mvc-template-engines)

### Guides
The following guides illustrate how to use some features concretely:

* [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa/)
* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/bookmarks/)
* [Handling Form Submission](https://spring.io/guides/gs/handling-form-submission/)

-->
