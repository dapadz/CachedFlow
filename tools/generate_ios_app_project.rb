#!/usr/bin/env ruby

require 'fileutils'
require 'xcodeproj'

ROOT = File.expand_path('..', __dir__)
IOS_APP_DIR = File.join(ROOT, 'iosApp')
PROJECT_PATH = File.join(IOS_APP_DIR, 'iosApp.xcodeproj')
PROJECT_NAME = 'iosApp'
FRAMEWORK_NAME = 'CachedFlowDemo.framework'
FRAMEWORK_DIR = 'KotlinFramework'
FRAMEWORK_PATH = File.join(FRAMEWORK_DIR, FRAMEWORK_NAME)

FileUtils.rm_rf(PROJECT_PATH)
project = Xcodeproj::Project.new(PROJECT_PATH)

target = project.new_target(:application, PROJECT_NAME, :ios, '17.2')

common_build_settings = {
  'PRODUCT_NAME' => '$(TARGET_NAME)',
  'PRODUCT_BUNDLE_IDENTIFIER' => 'ru.dapadz.cachedflow.iosApp',
  'SWIFT_VERSION' => '5.0',
  'DEVELOPMENT_TEAM' => '',
  'CODE_SIGN_STYLE' => 'Automatic',
  'CODE_SIGNING_ALLOWED[sdk=iphonesimulator*]' => 'NO',
  'CODE_SIGNING_REQUIRED[sdk=iphonesimulator*]' => 'NO',
  'IPHONEOS_DEPLOYMENT_TARGET' => '17.2',
  'TARGETED_DEVICE_FAMILY' => '1,2',
  'GENERATE_INFOPLIST_FILE' => 'YES',
  'INFOPLIST_KEY_CFBundleDisplayName' => 'CachedFlow Demo',
  'INFOPLIST_KEY_UIApplicationSceneManifest_Generation' => 'YES',
  'INFOPLIST_KEY_UIApplicationSupportsIndirectInputEvents' => 'YES',
  'INFOPLIST_KEY_UILaunchScreen_Generation' => 'YES',
  'LD_RUNPATH_SEARCH_PATHS' => '$(inherited) @executable_path/Frameworks',
  'FRAMEWORK_SEARCH_PATHS' => '$(inherited) $(SRCROOT)/KotlinFramework',
  'MARKETING_VERSION' => '1.0',
  'CURRENT_PROJECT_VERSION' => '1',
  'ENABLE_USER_SCRIPT_SANDBOXING' => 'NO'
}

target.build_configurations.each do |config|
  common_build_settings.each do |key, value|
    config.build_settings[key] = value
  end
end

main_group = project.main_group
sources_group = main_group.new_group(PROJECT_NAME)
framework_group = main_group.new_group(FRAMEWORK_DIR, FRAMEWORK_DIR)

%w[iosApp.swift ComposeRootView.swift].each do |file_name|
  file_ref = sources_group.new_file(file_name)
  target.source_build_phase.add_file_reference(file_ref)
end

framework_ref = framework_group.new_file(FRAMEWORK_NAME)
target.frameworks_build_phase.add_file_reference(framework_ref)

script = <<~'SCRIPT'
  set -euo pipefail

  cd "$SRCROOT/.."
  export JAVA_HOME="${JAVA_HOME:-$("/usr/libexec/java_home" -v 17)}"

  if [[ "${CONFIGURATION}" == Release* ]]; then
    BUILD_VARIANT="Release"
    FRAMEWORK_VARIANT_DIR="releaseFramework"
  else
    BUILD_VARIANT="Debug"
    FRAMEWORK_VARIANT_DIR="debugFramework"
  fi

  if [[ "${PLATFORM_NAME}" == *simulator* ]]; then
    if [[ "${ARCHS:-}" == *x86_64* && "${ARCHS:-}" != *arm64* ]]; then
      KONAN_TARGET="IosX64"
      FRAMEWORK_TARGET_DIR="iosX64"
    else
      KONAN_TARGET="IosSimulatorArm64"
      FRAMEWORK_TARGET_DIR="iosSimulatorArm64"
    fi
  else
    KONAN_TARGET="IosArm64"
    FRAMEWORK_TARGET_DIR="iosArm64"
  fi

  GRADLE_TASK=":app:link${BUILD_VARIANT}Framework${KONAN_TARGET}"
  FRAMEWORK_SOURCE="app/build/bin/${FRAMEWORK_TARGET_DIR}/${FRAMEWORK_VARIANT_DIR}/CachedFlowDemo.framework"
  FRAMEWORK_DEST="$SRCROOT/KotlinFramework"

  bash ./gradlew "$GRADLE_TASK"

  rm -rf "${FRAMEWORK_DEST}/CachedFlowDemo.framework"
  mkdir -p "${FRAMEWORK_DEST}"
  rsync -a "${FRAMEWORK_SOURCE}" "${FRAMEWORK_DEST}/"
  SCRIPT

script_phase = target.new_shell_script_build_phase('Build Kotlin Framework')
script_phase.shell_path = '/bin/bash'
script_phase.shell_script = script
script_phase.output_paths = ['$(SRCROOT)/KotlinFramework/CachedFlowDemo.framework/CachedFlowDemo']
target.build_phases.delete(script_phase)
target.build_phases.unshift(script_phase)

project.recreate_user_schemes
project.save
Xcodeproj::XCScheme.share_scheme(PROJECT_PATH, PROJECT_NAME)
