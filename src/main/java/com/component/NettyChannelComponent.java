package com.component;

import com.artemis.Component;
import io.netty.channel.Channel;

public class NettyChannelComponent extends Component {
    public Channel channel;

    public NettyChannelComponent() {}

    public NettyChannelComponent(Channel channel){
        this.channel = channel;
    }
}