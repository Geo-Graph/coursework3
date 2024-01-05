package coursework03;

import java.io.Serializable;

//- Этот класс представляет сообщение, которым обмениваются клиент и сервер.
//- Он реализует интерфейс Serializable, чтобы его объекты можно было передавать по сети.
public class Message implements Serializable
{
    private String content;

    public Message(String content)
    {
        this.content = content;
    }

    public String getContent()
    {
        return content;
    }
}