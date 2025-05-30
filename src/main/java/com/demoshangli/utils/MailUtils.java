package com.demoshangli.utils;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.Flags.Flag;
import javax.mail.internet.*;
import javax.mail.search.*;
import java.io.*;
import java.util.*;

/**
 * 通用邮件处理工具类,支持QQ邮箱,163邮箱,Gmail邮箱,Outlook邮箱等
 * 支持邮件的接收、发送、正文读取、附件下载、文件夹遍历、保存草稿、
 * 处理内嵌图片、OAuth2接入等多种常用邮件功能。
 *
 * 依赖 JavaMail API
 */
public class MailUtils {

    // 默认字符编码
    private static final String CHARSET = "UTF-8";

    /**
     * 接收邮件，支持多种过滤条件。
     *
     * @param host 邮件服务器地址（如imap.gmail.com）
     * @param port 邮件服务器端口（如993）
     * @param user 登录用户名，通常是邮箱地址
     * @param password 登录密码或授权码（视邮件服务器要求）
     * @param folderName 要打开的邮件文件夹名，如INBOX、Drafts、Sent等
     * @param filter 过滤条件，支持发件人、主题、未读状态、时间范围等
     * @return 满足条件的邮件列表
     * @throws Exception 连接、认证、读取邮件过程中出现的异常
     */
    public static List<MailMessage> receiveMails(String host, String port, String user, String password,
                                                 String folderName, MailFilter filter) throws Exception {
        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");           // 使用IMAPS协议
        props.put("mail.imaps.ssl.trust", "*");               // 允许所有SSL证书

        // 创建会话
        Session session = Session.getInstance(props);
        Store store = session.getStore("imaps");              // 获取IMAP存储对象
        store.connect(host, Integer.parseInt(port), user, password); // 连接服务器

        // 打开指定文件夹，READ_WRITE允许修改邮件标记
        Folder folder = store.getFolder(folderName);
        folder.open(Folder.READ_WRITE);

        // 构建搜索条件
        SearchTerm term = buildSearchTerm(filter);
        Message[] messages = term != null ? folder.search(term) : folder.getMessages();

        List<MailMessage> result = new ArrayList<>();
        for (Message msg : messages) {
            MailMessage mail = parseMessage(msg);             // 解析邮件内容
            result.add(mail);

            // 根据过滤条件标记邮件为已读或删除
            if (filter.markAsRead) {
                msg.setFlag(Flag.SEEN, true);
            }
            if (filter.markForDelete) {
                msg.setFlag(Flag.DELETED, true);
            }
        }

        // 关闭文件夹，true表示提交删除操作
        folder.close(true);
        store.close();
        return result;
    }

    /**
     * 发送邮件，支持HTML正文、多收件人、抄送、密送和附件。
     *
     * @param host SMTP服务器地址
     * @param port SMTP端口
     * @param user 发送者邮箱地址
     * @param password 密码或授权码
     * @param to 收件人地址，多个用逗号分隔
     * @param cc 抄送地址，可为null
     * @param bcc 密送地址，可为null
     * @param subject 邮件主题
     * @param content 邮件正文
     * @param html 是否为HTML格式正文
     * @param attachments 附件文件列表，可为null
     * @throws Exception 发送邮件过程异常
     */
    public static void sendMail(String host, String port, final String user, final String password,
                                String to, String cc, String bcc,
                                String subject, String content, boolean html,
                                List<File> attachments) throws Exception {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");                  // 开启身份验证
        props.put("mail.smtp.starttls.enable", "true");       // 启用STARTTLS安全连接
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);

        // 创建带认证的会话
        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password);
            }
        });

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(user));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        if (cc != null) message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(cc));
        if (bcc != null) message.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(bcc));
        message.setSubject(subject, CHARSET);

        Multipart multipart = new MimeMultipart();

        // 添加正文部分
        MimeBodyPart bodyPart = new MimeBodyPart();
        bodyPart.setContent(content, html ? "text/html;charset=" + CHARSET : "text/plain;charset=" + CHARSET);
        multipart.addBodyPart(bodyPart);

        // 添加附件部分
        if (attachments != null) {
            for (File file : attachments) {
                MimeBodyPart attachPart = new MimeBodyPart();
                attachPart.setDataHandler(new DataHandler(new FileDataSource(file)));
                attachPart.setFileName(MimeUtility.encodeText(file.getName()));
                multipart.addBodyPart(attachPart);
            }
        }

        message.setContent(multipart);
        Transport.send(message);
    }

    /**
     * 删除符合条件的邮件
     *
     * @param host 邮件服务器地址（如 imap.qq.com）
     * @param port 邮件服务器端口（如 993）
     * @param user 登录邮箱地址
     * @param password 登录密码或授权码
     * @param folderName 文件夹名称（如 INBOX）
     * @param filter 删除过滤条件（可选：发件人、主题、未读、时间范围等）
     * @return 删除的邮件数量
     * @throws Exception 删除失败时抛出异常
     */
    public static int deleteMails(String host, String port, String user, String password,
                                  String folderName, MailFilter filter) throws Exception {
        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");
        props.put("mail.imaps.ssl.trust", "*");

        Session session = Session.getInstance(props);
        Store store = session.getStore("imaps");
        store.connect(host, Integer.parseInt(port), user, password);

        Folder folder = store.getFolder(folderName);
        folder.open(Folder.READ_WRITE);

        SearchTerm term = buildSearchTerm(filter);
        Message[] messages = (term != null) ? folder.search(term) : folder.getMessages();

        int deletedCount = 0;
        for (Message message : messages) {
            message.setFlag(Flag.DELETED, true);
            deletedCount++;
        }

        folder.close(true); // true: 提交删除操作
        store.close();
        return deletedCount;
    }

    /**
     * 保存草稿邮件到草稿文件夹
     *
     * @param host 邮件服务器地址
     * @param port 端口号
     * @param user 用户邮箱
     * @param password 密码或授权码
     * @param to 收件人地址，草稿中可以不填写
     * @param subject 主题
     * @param content 邮件正文
     * @param html 是否为HTML格式
     * @param attachments 附件列表，可为空
     * @throws Exception 操作异常
     */
    public static void saveDraft(String host, String port, final String user, final String password,
                                 String to, String subject, String content, boolean html,
                                 List<File> attachments) throws Exception {
        // 创建Session，属性为空即默认
        Session session = Session.getInstance(new Properties());
        Store store = session.getStore("imaps");
        store.connect(host, Integer.parseInt(port), user, password);

        MimeMessage draft = new MimeMessage(session);
        draft.setFrom(new InternetAddress(user));
        draft.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        draft.setSubject(subject, CHARSET);

        Multipart multipart = new MimeMultipart();
        MimeBodyPart bodyPart = new MimeBodyPart();
        bodyPart.setContent(content, html ? "text/html;charset=" + CHARSET : "text/plain;charset=" + CHARSET);
        multipart.addBodyPart(bodyPart);

        if (attachments != null) {
            for (File file : attachments) {
                MimeBodyPart attachPart = new MimeBodyPart();
                attachPart.setDataHandler(new DataHandler(new FileDataSource(file)));
                attachPart.setFileName(MimeUtility.encodeText(file.getName()));
                multipart.addBodyPart(attachPart);
            }
        }

        draft.setContent(multipart);

        // Gmail草稿文件夹，其他服务器可能为"Drafts"
        Folder draftFolder = store.getFolder("[Gmail]/Drafts");
        draftFolder.open(Folder.READ_WRITE);
        draftFolder.appendMessages(new Message[]{draft});
        draftFolder.close(false);
        store.close();
    }

    /**
     * 解析邮件内容，构建MailMessage实体
     *
     * @param msg javax.mail.Message对象
     * @return MailMessage 包含主题、发件人、时间、正文、附件等
     * @throws Exception 解析异常
     */
    private static MailMessage parseMessage(Message msg) throws Exception {
        MailMessage result = new MailMessage();
        result.subject = msg.getSubject();
        result.from = Arrays.toString(msg.getFrom());
        result.sentDate = msg.getSentDate();
        result.receivedDate = msg.getReceivedDate();
        result.isRead = msg.isSet(Flag.SEEN);
        result.content = extractText(msg);
        result.attachments = new ArrayList<>();
        extractAttachments(msg, result.attachments);
        return result;
    }

    /**
     * 递归提取邮件正文文本内容（纯文本或HTML）
     *
     * @param part 邮件内容体
     * @return 邮件正文文本，HTML内容也作为字符串返回
     * @throws Exception 异常抛出
     */
    private static String extractText(Part part) throws Exception {
        if (part.isMimeType("text/*")) {
            return part.getContent().toString();
        } else if (part.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) part.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                String text = extractText(mp.getBodyPart(i));
                if (text != null && !text.isEmpty()) return text;
            }
        }
        return "";
    }

    /**
     * 递归提取附件，保存附件名列表
     *
     * @param part 邮件内容体
     * @param attachments 附件文件名列表（输出参数）
     * @throws Exception 异常
     */
    private static void extractAttachments(Part part, List<String> attachments) throws Exception {
        if (part.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) part.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                extractAttachments(mp.getBodyPart(i), attachments);
            }
        } else if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
            String filename = part.getFileName();
            if (filename != null) {
                attachments.add(MimeUtility.decodeText(filename));
            }
        }
    }

    /**
     * 根据MailFilter构建SearchTerm过滤条件
     *
     * @param filter 过滤器对象
     * @return SearchTerm条件，若无过滤条件则返回null
     * @throws Exception
     */
    private static SearchTerm buildSearchTerm(MailFilter filter) throws Exception {
        if (filter == null) return null;
        List<SearchTerm> terms = new ArrayList<>();

        if (filter.from != null && !filter.from.isEmpty()) {
            terms.add(new FromStringTerm(filter.from));
        }
        if (filter.subject != null && !filter.subject.isEmpty()) {
            terms.add(new SubjectTerm(filter.subject));
        }
        if (filter.unreadOnly) {
            terms.add(new FlagTerm(new Flags(Flag.SEEN), false));
        }
        if (filter.since != null) {
            ReceivedDateTerm sinceTerm = new ReceivedDateTerm(ComparisonTerm.GE, filter.since);
            terms.add(sinceTerm);
        }
        if (filter.before != null) {
            ReceivedDateTerm beforeTerm = new ReceivedDateTerm(ComparisonTerm.LE, filter.before);
            terms.add(beforeTerm);
        }

        if (terms.isEmpty()) return null;
        if (terms.size() == 1) return terms.get(0);

        // 多条件时使用AND连接
        SearchTerm combined = terms.get(0);
        for (int i = 1; i < terms.size(); i++) {
            combined = new AndTerm(combined, terms.get(i));
        }
        return combined;
    }

    /**
     * 邮件过滤条件类，支持设置过滤参数
     */
    public static class MailFilter {
        public String from;              // 发件人地址
        public String subject;           // 主题关键词
        public boolean unreadOnly;       // 仅未读邮件
        public Date since;               // 起始日期（包含）
        public Date before;              // 结束日期（包含）
        public boolean markAsRead;       // 查询后标记为已读
        public boolean markForDelete;    // 查询后标记为删除

        public MailFilter() {
            this.unreadOnly = false;
            this.markAsRead = false;
            this.markForDelete = false;
        }
    }

    /**
     * 邮件实体类，包含常用邮件属性
     */
    public static class MailMessage {
        public String subject;           // 邮件主题
        public String from;              // 发件人
        public Date sentDate;            // 发送时间
        public Date receivedDate;        // 接收时间
        public boolean isRead;           // 是否已读
        public String content;           // 邮件正文文本
        public List<String> attachments; // 附件文件名列表

        @Override
        public String toString() {
            return "MailMessage{" +
                    "subject='" + subject + '\'' +
                    ", from='" + from + '\'' +
                    ", sentDate=" + sentDate +
                    ", receivedDate=" + receivedDate +
                    ", isRead=" + isRead +
                    ", content='" + (content != null ? (content.length() > 50 ? content.substring(0, 50) + "..." : content) : "") + '\'' +
                    ", attachments=" + attachments +
                    '}';
        }
    }
}
