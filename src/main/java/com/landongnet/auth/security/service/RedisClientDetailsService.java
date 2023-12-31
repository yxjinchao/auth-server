package com.landongnet.auth.security.service;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSONObject;
import com.github.snake.rock.redis.service.ICacheService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author snake
 * @description
 * @since 2023/8/17 16:16
 */
@Slf4j
@Service
public class RedisClientDetailsService extends JdbcClientDetailsService {
    /**
     * 缓存 client的 redis key，这里是 hash结构存储
     */
    private static final String CACHE_CLIENT_KEY = "client_details";

    private final ICacheService cacheService;

    public RedisClientDetailsService(DataSource dataSource, ICacheService cacheService) {
        super(dataSource);
        this.cacheService = cacheService;
    }

    @Override
    public ClientDetails loadClientByClientId(String clientId) throws InvalidClientException {
        ClientDetails clientDetails = null;
        String value = (String) cacheService.getHash(CACHE_CLIENT_KEY, clientId);
        if (StringUtils.isBlank(value)) {
            clientDetails = cacheAndGetClient(clientId);
        } else {
            clientDetails = JSONObject.parseObject(value, BaseClientDetails.class);
        }

        return clientDetails;
    }

    /**
     * 缓存 client并返回 client
     *
     * @param clientId clientId
     */
    public ClientDetails cacheAndGetClient(String clientId) {
        ClientDetails clientDetails = null;
        clientDetails = super.loadClientByClientId(clientId);
        if (clientDetails != null) {
            BaseClientDetails baseClientDetails = (BaseClientDetails) clientDetails;
            Set<String> autoApproveScopes = baseClientDetails.getAutoApproveScopes();
            if (CollUtil.isNotEmpty(autoApproveScopes)) {
                baseClientDetails.setAutoApproveScopes(
                        autoApproveScopes.stream().map(this::convert).collect(Collectors.toSet())
                );
            }
            cacheService.setHash(CACHE_CLIENT_KEY, clientId, JSONObject.toJSONString(baseClientDetails));
        }
        return clientDetails;
    }

    /**
     * 删除 redis缓存
     *
     * @param clientId clientId
     */
    public void removeRedisCache(String clientId) {
        cacheService.deleteHashField(CACHE_CLIENT_KEY, clientId);
    }

    /**
     * 将 oauth_client_details全表刷入 redis
     */
    public void loadAllClientToCache() {
        if (cacheService.hasKey(CACHE_CLIENT_KEY)) {
            return;
        }
        log.info("将oauth_client_details全表刷入redis");

        List<ClientDetails> list = super.listClientDetails();
        if (CollUtil.isEmpty(list)) {
            log.error("oauth_client_details表数据为空，请检查");
            return;
        }
        list.forEach(client -> cacheService.setHash(CACHE_CLIENT_KEY, client.getClientId(), JSONObject.toJSONString(client)));
    }

    private String convert(String value) {
        final String logicTrue = "1";
        return logicTrue.equals(value) ? Boolean.TRUE.toString() : Boolean.FALSE.toString();
    }
}

