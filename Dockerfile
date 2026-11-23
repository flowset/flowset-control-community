#
# Copyright (c) Haulmont 2024. All Rights Reserved.
# Use is subject to license terms.
#

FROM amazoncorretto:21.0.13 AS layers

COPY build/libs/*.jar app.jar

ENTRYPOINT ["java","-Dcom.sun.net.ssl.checkRevocation=false","-jar","/app.jar"]