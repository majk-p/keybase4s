package net.michalp.keybase.transport

object syntax {
    implicit class MessageWrapperOps(m: response.MessageWrapper) {
        def sender: String = m.msg.sender.username
        def channel: String = m.msg.channel.name
        def content: String = m.msg.content.text.body
    }
}