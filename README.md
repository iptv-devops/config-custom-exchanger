# Config Exchanger

This library is for exchanging the config value written by specific rules.

The Exchanger can exist various type.
For enable each Exchanger, enable parameter have to set as true.

#### _Example_
```yml
config-exchanger:
  aws-parameter-store:
    enabled: true
```
Above setting is for enabling 'aws-parameter-store' Exchanger.

## aws-parameter-store

| Parameter | Default                                                                                  |
|-----------|------------------------------------------------------------------------------------------|
| Region    | This is optional value.<br/>if not set, Region info is not used when created AWS client. |


#### _Example for using fixed region_
```yml
config-exchanger:
  aws-parameter-store:
    enabled: true
    region: ap-northeast-2
```

below setting value will be exchanged the value of key "/dev/serviceA/db_password" on "aws_parameter_store"

#### _Example_
```yml
database:
    password: "{aws_parameter_store}/dev/serviceA/db_password"

```

### Publish repository info 

>https://github.com/iptv-devops/maven-repository