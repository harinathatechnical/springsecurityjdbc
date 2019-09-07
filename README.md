# springsecurityjdbc

# create maven project and add the following dependencies in pom 

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.1.3.RELEASE</version>
        <relativePath></relativePath>
    </parent>
    
         <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-jdbc</artifactId>
        </dependency>
        <dependency>
             <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>


       <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
    
    
# create Home Controller 
    
      package com.test;

        import org.springframework.web.bind.annotation.GetMapping;
        import org.springframework.web.bind.annotation.RestController;

        @RestController
        public class HomeResource {

            /**
             * this is able to access by everyone
             * @return
             */
            @GetMapping("/")
            public String home(){
                return ("<h1>Welcome</h1>");
            }

            /**
             * this is able to access by only admin
             * @return
             */
            @GetMapping("/admin")
            public String admin(){
                return ("<h1>Welcome Admin</h1>");
            }

            /**
             * this is able to access by admin and user
             * @return
             */
            @GetMapping("/user")
            public String user(){
                return ("<h1>Welcome User</h1>");
            }
        }

# Main class 

      package com.test;

      import org.springframework.boot.SpringApplication;
      import org.springframework.boot.autoconfigure.SpringBootApplication;

      @SpringBootApplication
      public class SpringBootSecurityApplication {

          public static void main(String []args){
              SpringApplication.run(SpringBootSecurityApplication.class,args);
          }
      }

# create SecurityConfiguration class 
        
        the following is the default security schema load on start up and created data (https://docs.spring.io/spring-security/site/docs/current/reference/htmlsingle/#user-schema)

          configure default H2 data base as it contains in class path
          
          package com.test;


            import org.springframework.beans.factory.annotation.Autowired;
            import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
            import org.springframework.security.config.annotation.web.builders.HttpSecurity;
            import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
            import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
            import org.springframework.security.core.userdetails.User;

            import javax.sql.DataSource;

            @EnableWebSecurity
            public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

                @Autowired
                DataSource dataSource;

                @Override
                protected void configure(AuthenticationManagerBuilder auth) throws Exception {
                    auth.jdbcAuthentication()
                    .dataSource(dataSource)
                    .withDefaultSchema()
                    .withUser(
                            User.withUsername("user")
                            .password("pass")
                            .roles("USER")
                    ).withUser(
                            User.withUsername("admin")
                                    .password("pass")
                                    .roles("ADMIN")
                    );

                }

                @Override
                public void configure(HttpSecurity http) throws Exception {
                    http.authorizeRequests()
                            .antMatchers("/admin").hasAnyRole("ADMIN")
                            .antMatchers("/user").hasAnyRole("USER","ADMIN")
                            .antMatchers("/").permitAll()
                            .and().formLogin();

                }
                
                @Bean
                public PasswordEncoder getPasswordEncoder()
                {
                    return NoOpPasswordEncoder.getInstance();
                }
            }

# Remove default schema and default users as part of configure 

        In real world we have already exisint schema and data to use the authentication we need to say to spring security.
        create default schema default which is spring security use (https://docs.spring.io/spring-security/site/docs/current/reference/htmlsingle/#user-schema) 
        
        now the configure method looks like 
        
        @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.jdbcAuthentication()
        .dataSource(dataSource);

      }
        
     create schema.sql (which is refer by spring boot default and run before application starts )
        
        create table users(
    username varchar_ignorecase(50) not null primary key,
    password varchar_ignorecase(50) not null,
    enabled boolean not null
    );

    create table authorities (
        username varchar_ignorecase(50) not null,
        authority varchar_ignorecase(50) not null,
        constraint fk_authorities_users foreign key(username) references users(username)
    );
    create unique index ix_auth_username on authorities (username,authority);
        
    create data.sql (actual data of the users )
    INSERT INTO users (username,password,enabled)
    values('user','pass',true);

    INSERT INTO users (username,password,enabled)
    values('admin','pass',true);
      
     INSERT INTO authorities (username,authority)
     values ('user','ROLE_USER');

     INSERT INTO authorities (username,authority)
     values ('admin','ROLE_ADMIN'); 
          
# works with real schema and users 

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.jdbcAuthentication()
        .dataSource(dataSource)
        .usersByUsernameQuery("select username,password,enabled "
                +"from users "
                +"where username = ? ")
        .authoritiesByUsernameQuery("select username,authority " +
                "from authorities " +
                "where username = ? ");

    }
    
    u can changes the query and schemas like table name changes as per requirement 
    in application.properties use the following to configure different data bases like mysql / oracle .
    
    spring.datasource.url=
    spring.datasource.username=
    spring.datasource.password=
    
