const { faker } = require('@faker-js/faker');

module.exports = {
    generateRandomName: function (userContext, events, done) {
        userContext.vars.randomFirstName = faker.name.firstName();
        userContext.vars.randomLastName = faker.name.lastName();
        userContext.vars.randomEmail = faker.internet.email(
            userContext.vars.randomFirstName,
            userContext.vars.randomLastName,
            'workshop.demo'
        );
        return done();
    },
};
