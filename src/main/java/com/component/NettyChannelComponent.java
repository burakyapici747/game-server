package com.component;

import com.artemis.Component;
import io.netty.channel.Channel;

public class NettyChannelComponent extends Component {
    public Channel channel;
    public Integer lastProcessedSequenceId;

    public NettyChannelComponent() {}

    public NettyChannelComponent(Channel channel, Integer lastProcessedSequenceId){
        this.channel = channel;
        this.lastProcessedSequenceId = lastProcessedSequenceId;
    }
}
