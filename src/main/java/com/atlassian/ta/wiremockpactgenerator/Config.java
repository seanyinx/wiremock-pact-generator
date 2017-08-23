package com.atlassian.ta.wiremockpactgenerator;

public class Config {
    private String consumerName;
    private String providerName;
    private FileSystem fileSystem;
    private IdGenerator idGenerator;

    private Config() {
    }

    public String getConsumerName() {
        return consumerName;
    }

    public String getProviderName() {
        return providerName;
    }

    public FileSystem getFileSystem() {
        return fileSystem;
    }

    public IdGenerator getIdGenerator() {
        return idGenerator;
    }

    public static class Builder {
        private String consumerName;
        private String providerName;
        private FileSystem fileSystem;
        private IdGenerator idGenerator;

        public Builder() {

        }

        private Builder(final String consumerName, final String providerName,
                        final FileSystem fileSystem, final IdGenerator idGenerator) {
            this.consumerName = consumerName;
            this.providerName = providerName;
            this.fileSystem = fileSystem;
            this.idGenerator = idGenerator;
        }

        public Builder withConsumerName(final String consumerName) {
            return new Builder(consumerName, providerName, fileSystem, idGenerator);
        }

        public Builder withProviderName(final String providerName) {
            return new Builder(consumerName, providerName, fileSystem, idGenerator);
        }

        public Builder withFileSystem(final FileSystem fileSystem) {
            return new Builder(consumerName, providerName, fileSystem, idGenerator);
        }

        public Builder withIdGenerator(final IdGenerator idGenerator) {
            return new Builder(consumerName, providerName, fileSystem, idGenerator);
        }

        public Config build() {
            final Config config = new Config();
            config.consumerName = consumerName;
            config.providerName = providerName;
            config.fileSystem = fileSystem;
            config.idGenerator = idGenerator;
            return config;
        }
    }
}
