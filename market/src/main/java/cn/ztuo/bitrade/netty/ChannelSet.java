package cn.ztuo.bitrade.netty;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

@Slf4j
@Component
public class ChannelSet {
    private Set<Channel> channelSet = new LinkedHashSet<>();

    public boolean addChannel(Channel channel){
        log.info("new channel,id="+channel.id());
        return this.channelSet.add(channel);
    }
    public boolean removeChannel(Channel channel){
        Iterator<Channel> iterator =  channelSet.iterator();
        while (iterator.hasNext()){
            Channel item = iterator.next();
            if(item.id().asLongText().equalsIgnoreCase(channel.id().asLongText())){
                iterator.remove();
                log.info("remove channel,id="+channel.id());
                return true;
            }
        }
        return false;
    }

    public Set<Channel> getChannels(){
        return this.channelSet;
    }
}
