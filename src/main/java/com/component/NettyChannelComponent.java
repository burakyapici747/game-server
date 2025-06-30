package com.component;

import com.artemis.Component;
import io.netty.channel.Channel;

public class NettyChannelComponent extends Component {
    public Channel channel;
    public int lastProcessedSequenceId;

    public NettyChannelComponent() {}

    public NettyChannelComponent(Channel channel, int lastProcessedSequenceId){
        this.channel = channel;
        this.lastProcessedSequenceId = lastProcessedSequenceId;
    }
}