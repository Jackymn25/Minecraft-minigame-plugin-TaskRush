package me.jacky.taskMaster.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReloadPluginTabCompleter implements TabCompleter {

    // 支持的语言列表
    private static final List<String> LANGUAGES = Arrays.asList(
            "zh_cn",  // 简体中文
            "en_us",  // 美式英语
            "ja_jp",  // 日语
            "ko_kr",  // 韩语
            "fr_fr"  // 法语
    );

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command,
                                      String alias, String[] args) {

        final List<String> completions = new ArrayList<>();

        // 当输入第一个参数时
        if (args.length == 1) {
            // 使用 StringUtil.copyPartialMatches 进行模糊匹配
            StringUtil.copyPartialMatches(args[0], LANGUAGES, completions);
        }

        // 如果超过一个参数，不提供补全
        return completions;
    }
}
