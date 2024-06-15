package com.xiaohe.nacos.common.packagescan.resource;

import com.xiaohe.nacos.common.packagescan.resource.ResourceLoader;


@FunctionalInterface
public interface ProtocolResolver {

    /**
     * Resolve the given location against the given resource loader
     * if this implementation's protocol matches.
     *
     * @param location       the user-specified resource location
     * @param resourceLoader the associated resource loader
     * @return a corresponding {@code Resource} handle if the given location
     * matches this resolver's protocol, or {@code null} otherwise
     */
    Resource resolve(String location, ResourceLoader resourceLoader);

}
