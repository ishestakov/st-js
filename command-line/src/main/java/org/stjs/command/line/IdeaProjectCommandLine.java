package org.stjs.command.line;

import static com.google.common.collect.Lists.newArrayList;

import java.io.File;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.CharMatcher;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.base.StandardSystemProperty;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;

public class IdeaProjectCommandLine {
	private static final Function<String, Iterable<File>> TO_FILE_FUNCTION = new Function<String, Iterable<File>>() {
		@Nullable
		@Override
		public Iterable<File> apply(String s) {
			File file = new File(s);
			if (file.isDirectory()) {
				return listFiles(file, ".jar");
			}
			return Collections.singleton(file);
		}
	};
	public static final Function<String, String> REMOVE_WILDARD_FUNCTION = new Function<String, String>() {
		@Nullable
		@Override
		public String apply(@Nullable String s) {
			return CharMatcher.is('*').trimTrailingFrom(s);
		}
	};

	public static void main(String[] args) {
		if (args.length != 3) {
			System.err.println("Usage: st-js <srcDir> <classPathStr> <outputDir>");
			return;
		}
		String path = args[0];
		if (!path.endsWith(File.separator)) {
			path += File.separator;
		}
		String classPath = args[1];
		String pathSeparator = System.getProperty(StandardSystemProperty.PATH_SEPARATOR.key());
		Iterable<String> libs = Splitter.on(pathSeparator).trimResults().omitEmptyStrings().split(classPath);

		List<File> dependencies = FluentIterable.from(libs).transform(REMOVE_WILDARD_FUNCTION).transformAndConcat(TO_FILE_FUNCTION).toList();

		String outputDir = args[2];
		List<File> classNames = listFiles(new File(path), ".java");

		CommandLine.compile(path, classNames, dependencies);
		generate(path, classNames, dependencies, outputDir);

	}

	private static void generate(String path, List<File> files, List<File> dependencies, String outputDir) {
		File srcPath = new File(path);
		for (File file : files) {
			// remove the leading srcPath from each file to get the source name
			CommandLine.generate(
					path,
					file.getAbsolutePath().substring(srcPath.getAbsolutePath().length() + 1).replace(".java", "")
							.replace(File.separatorChar, '.'), dependencies, outputDir);
		}
	}

	private static List<File> listFiles(File srcDir, String suffix) {
		List<File> files = newArrayList();
		listFiles0(srcDir, files, suffix);
		return files;
	}

	private static void listFiles0(File srcDir, List<File> output, String suffix) {
		for (File file : srcDir.listFiles()) {
			if (file.isDirectory()) {
				listFiles0(file, output, suffix);
			} else {
				if (file.getName().toUpperCase().endsWith(suffix.toUpperCase())) {
					output.add(file);
				}
			}
		}
	}
}
