package com.pszymczyk.consul.infrastructure.client

import groovyx.net.http.ContentType
import groovyx.net.http.EncoderRegistry
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.RESTClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class SimpleConsulClient {

    private static final String NO_LEADER_ELECTED_RESPONSE = ""

    private final RESTClient http

    SimpleConsulClient(RESTClient http) {
        this.http = http
    }

    boolean isLeaderElected() {
        HttpResponseDecorator response = http.get(path: '/v1/status/leader', contentType: ContentType.JSON)

        response.getData() != NO_LEADER_ELECTED_RESPONSE
    }

    Collection getRegisteredNodes() {
        HttpResponseDecorator response = http.get(path: '/v1/catalog/nodes', contentType: ContentType.JSON)

        response.getData()
    }

    Collection<String> getServicesIds() {
        HttpResponseDecorator response = http.get(path: '/v1/agent/services', contentType: ContentType.JSON)

        response.getData()
                .keySet()
                .findAll({ it -> it != 'consul' })
    }

    void deregister(String id) {
        http.put(path: "/v1/agent/service/deregister/$id", contentType: ContentType.ANY)
    }

    void clearKvStore() {
        http.delete(path: "/v1/kv/", query: [recurse: true], contentType: ContentType.ANY)
    }

    void destroyActiveSessions() {
        HttpResponseDecorator response = http.get(path: "/v1/session/list", contentType: ContentType.JSON)

        response.getData().each {
            def id = it.ID
            http.put(path: "/v1/session/destroy/$id", contentType:  ContentType.ANY)
        }
    }

    void deregisterAllChecks() {
        HttpResponseDecorator response = http.get(path: "/v1/agent/checks", contentType: ContentType.JSON)

        response.getData().each {
            def id = it.key

            http.put(path: "/v1/agent/check/deregister/$id", contentType: ContentType.ANY)
        }
    }
}
