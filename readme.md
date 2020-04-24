# 제3회 IT인프라 엔지니어 밋업 실습 자료

## 이 자료에 포함 된 것

1. 실습용 가상머신 설정을 위한 Vagrant 파일(VirtualBox와 연계해 리눅스 가상 머신을 생성 해 줌)
1. Prometheus + Grafana + cAdvisor + node_exporter + Spring Boot App이 포함된 docker-compose.yml 파일 (실습 환경을 컨테이너 기반으로 생성 해 줌)
3. docker를 인스톨 하기 위한 설치 스크립트 (docker-install.sh)
4. 리눅스 VM에 접속하기 위한 Putty Private Key 파일 (common_private.ppk)

## 이 자료를 활용하기 위해 사용자의 PC에 필요한 것

1. <a href="https://www.virtualbox.org/">VirtualBox</a> - 가상머신을 구동할 수 있게 해 줌
2. <a href="https://www.vagrantup.com/">Vagrant</a> - VirtualBox를 이용한 가상머신 생성을 쉽게 해 줌
3. <a href="https://git-scm.com/">git</a> - 소스코드 저장소에서 소스를 내려 받는 용도
4. <a href="https://www.chiark.greenend.org.uk/~sgtatham/putty/latest.html">putty</a> - 가상머신에 터미널로 접속하게 해 줌 

## 이 자료로 환경을 구성하는 순서

1. VirtualBox와 Vagrant, git을 설치하고
2. `git clone https://github.com/gnu-gnu/infra-meetup.git`을 임의의 디렉토리에서 실행시키면 /infra-meetup 디렉토리가 생성됨
3. 해당 디렉토리로 이동하여 vagrant up 을 실행하면 VirtualBox에 gnu 라는 이름의 가상머신이 접속됨
4. putty를 설치하고 putty에 포함된 pageant라는 프로그램을 실행하여 common_private.ppk를 등록하거나, putty에 localhost:2222 를 접속 대상으로 설정할 때 해당 키를 Auth 수단으로 등록하고 접속
5. ssh 접속 ID/PW는 vagrant/vagrant, 내부에서 su - 로 root 유저로 접속할 때도 비밀번호는 vagrant 로 설정되어 있음
6. putty 로 localhost:2222 로 접속한 후 `yum instal -y git`으로 git 설치 (이 리포지토리가 Linux 내부에도 다운된다)
7. `/infra-meetup` 디렉토리로 이동하여 `sh docker-install.sh`를 수행하면 docker 및 docker-compose 가 설치된다.
8. `docker-compose.yml`이 위치한 디렉토리에서 `docker-compose up -d`를 수행하면 구성된 환경이 구동된다.

## 각 구성 요소에 대한 소개

### 각 구성요소가 사용하는 포트
1. 3000 : Grafana (대시보드)
2. 8080 : cAdvisor (컨테이너 메트릭 노출)
3. 8888 : Java 애플리케이션
4. 9090 : 프로메테우스 (모니터링 도구)
5. 9100 : node-exporter (설치된 서버의 정보를 수집)

### 애플리케이션
```xml
<!-- pom.xml -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <dependency>
            <groupId>io.micrometer</groupId>
            <artifactId>micrometer-registry-prometheus</artifactId>
            <version>1.3.7</version>
        </dependency>
```
`pom.xml` 에서 위의 의존성을 추가 해 주는 것이 스프링 부트 애플리케이션을 프로메테우스로 구성하는데 필요한 요소이다. micrometer-registery-prometheus는 현재 1.4.x 버전까지 릴리즈 되어 있으나 발표자의 테스트 결과, 현 환경(2.2.6.RELEASE)와 함께 사용시 No Such Method 와 같은 Exception이 간혹 발생하였다.

```properties
# application.properties
management.endpoints.web.exposure.include=*
management.endpoints.web.exposure.exclude=env,beans
```
스프링부트의 구동과 관련된 환경 설정에서 구성 조회를 위한 web endpoint를 노출시켜주는 설정이다. 모든 Endpoint를 노출하되, 환경과 Bean에 관한 설정은 숨기는 설정이다. Prometheus와 관련된 Endpoint는 기본적으로 Web에서는 노출되지 않으므로 이 설정이 필요하다.

```xml
            <plugin>
                <groupId>com.google.cloud.tools</groupId>
                <artifactId>jib-maven-plugin</artifactId>
                <version>2.2.0</version>
                <configuration>
                    <to>
                        <image>webfuel/infra-meetup-sample</image>
                        <auth>
                            <username></username>
                            <password></password>
                        </auth>
                    </to>
                    <container>
                        <ports>
                            <port>8888</port>
                        </ports>
                    </container>
                </configuration>
            </plugin>
```
만약 애플리케이션을 직접 컨테이너로 빌드하고 싶다면 위의 `<image>` 태그의 부분을 본인의 Docker registry 주소로 수정하고, `<auth>` 태그에 인증 정보를 입력한 후 `jib:build`와 같은 GOAL을 Maven으로 수행 해 주면 된다. 단 jib은 사전에 compile된 소스에 대해 동작하기 때문에 `mvn clean compile jib:build`와 같은 실행이 필요하다. 빌드를 마치면 지정한 registry로 push된다.

애플리케이션이 구동되면 `http://localhost:8888/actuator/prometheus` Endpoint를 통해 Metric을 노출한다.

### docker-compose.yml
복수의 docker 컨테이너를 구동시키는 도구이다. 여러 개의 docker에 각종 설정을 적용하여 구동할 수 있고 및 여러 컨테이너의 내부 네트워크 설정, 볼륨 설정 등을 한 번에 할 수 있다. 각 섹션별 설명은 본문에 주석으로 포함한다.
```yaml
# docker-compose 파일의 버전, 버전별 호환성은 https://docs.docker.com/compose/compose-file/compose-versioning/ 참조
# 3.8 은 Docker Engine 19.03 이상의 버전에 대응한다.
version: '3.8'

# 컨테이너 내부의 파일은 호스트와 격리되어 있기 때문에 volume을 지정하여 호스트의 경로를 통해 공유할 수 있도록 한다.
# prometheus의 data가 저장될 volume과 grafana의 data가 저장될 volume을 지정한다
# 이 볼륨이 없다면 prometheus와 grafana는 새로 기동할 때마다 내부에 보관한 데이터를 잃어버리게 될 것이다.
volumes:
  prometheus_data: {}
  grafana_data: {}

# 내부적으로 네트워크를 내-외부로 분리된 것을 모사한다.
networks:
  front-tier:
  back-tier:

# 구동될 컨테이너들을 서비스로 표현한다.
services:
# prometheus 서비스를 정의하는 섹션이다.
  prometheus:
    image: prom/prometheus:v2.17.2
# prometheus 의 설정은 호스트의 /root/prometheus/prometheus.yml 경로에서 컨테이너 내부의 /etc/prometheus/prometheus.yml
# 경로로 연결하여 사용한다.
# /root/prometheus/prometheus.yml 경로에 해당 파일이 위치해야 한다. 
# 다른 경로에 이 파일이 있다면 해당 경로로 값을 수정해야 한다.
# 컨테이너 내부의 /prometheus 디렉토리는 호스트의 prometheus_data 볼륨과 연결한다.
    volumes:
      - /root/prometheus/prometheus.yml:/etc/prometheus/prometheus.yml
      - prometheus_data:/prometheus
# prometheus 실행 커맨드에 설정 파일의 위치 (위에서 지정해 준 것) 와 데이터를 저장할 위치 (위에서 지정한 볼륨과 연결된 위치)를 
# 지정한다. 기본 설정으로 사용하면 데이터가 prometheus 하위 디렉토리로 지정된다. 
# 실제 운영환경에서는 NAS 혹은 용량이 충분히 확보된 Partition으로 연결 시켜주는 것이 중요하다
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'
# 9090 포트를 노출한다.
    ports:
      - 9090:9090
# cadvisor가 구동되었을 때 프로메테우스를 구동한다.
    depends_on:
      - cadvisor
# 네트워크는 backend 대역으로 가정한다.
    networks:
      - back-tier
# 컨테이너가 중지되었을 때 자동으로 재시작한다.
    restart: always
# 자바 애플리케이션
  springboot:
    image: webfuel/infra-meetup-sample
    ports:
      - 8888:8888
    networks:
      - back-tier

# node-exporter는 호스트 머신의 정보를 수집하여 prometheus로 전송하는 Agent이다.
# 원래 이것은 host에 직접 설치하는 것도 적절하지만, 환경 구성의 편의를 위해 컨테이너로 생성하고, 호스트의 시스템 정보와 관련된 
# 디렉토리를 호스트 내부로 연결해 사용한다.
  node-exporter:
    image: prom/node-exporter
# /proc : Kernel, Process 정보를 담고 있다.
# /sys : 하드웨어 정보를 담고 있다.
#/ : 파일시스템 루트이다.
# 호스트의 위의 경로를 컨테이너 내부에 연결하여 node-exporter가 호스트의 정보를 노출할 수 있도록 한다 :ro는 Read-only 이다.
# 당연히 컨테이너 주제에 호스트의 정보를 맘대로 고치면 안 되는 것은 당연한 이치이다ㅣ.
    volumes:
      - /proc:/host/proc:ro
      - /sys:/host/sys:ro
      - /:/rootfs:ro
# node-exporter가 수집할 proc과 sys위치를 지정해주는 옵션을 설정한다.
# 이 node-exporter는 호스트의 정보를 수집하기 때문에 무시해도 될 정보는 정규식으로 입력한다
    command:
      - '--path.procfs=/host/proc'
      - '--path.sysfs=/host/sys'
      - --collector.filesystem.ignored-mount-points
      - "^/(sys|proc|dev|host|etc|rootfs/var/lib/docker/containers|rootfs/var/lib/docker/overlay2|rootfs/run/docker/netns|rootfs/var/lib/docker/aufs)($$|/)"
    ports:
      - 9100:9100
    networks:
      - back-tier
    restart: always
# 구동 중인 컨테이너의 자원 사용량, 성능 메트릭을 노출시켜주는 cAdvisor 이다.
# 기본적으로 docker 의 정보를 수집하는데 최적화 되어 있으므로 k8s를 사용할 경우 각 노드에서 사용 중인 docker의 정보를 
# 수집할 수 있도록 DaemonSet으로 배포하는 전략이 유용하다
  cadvisor:
    image: google/cadvisor
    volumes:
      - /:/rootfs:ro
      - /var/run:/var/run:rw
      - /sys:/sys:ro
      - /var/lib/docker/:/var/lib/docker:ro
    ports:
      - 8080:8080
    networks:
      - back-tier
    restart: always
    deploy:
      mode: global

  grafana:
    image: grafana/grafana
    user: "472"
    depends_on:
      - prometheus
    ports:
      - 3000:3000
    volumes:
      - grafana_data:/var/lib/grafana
    networks:
      - back-tier
      - front-tier
    restart: always
```