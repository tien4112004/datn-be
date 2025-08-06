// MongoDB initialization script for presentation service

// Switch to the presentation database
db = db.getSiblingDB('presentationdb');

// Create a user with read/write permissions
db.createUser({
  user: 'presentation_user',
  pwd: 'presentation_password',
  roles: [
    {
      role: 'readWrite',
      db: 'presentationdb'
    }
  ]
});

// Create the presentations collection with validation schema
db.createCollection('presentations', {
  validator: {
    $jsonSchema: {
      bsonType: 'object',
      required: ['title', 'slides', 'createdAt', 'updatedAt'],
      properties: {
        title: {
          bsonType: 'string',
          description: 'must be a string and is required'
        },
        slides: {
          bsonType: 'array',
          description: 'must be an array and is required',
          items: {
            bsonType: 'object',
            required: ['id'],
            properties: {
              id: {
                bsonType: 'string',
                description: 'must be a string and is required'
              },
              elements: {
                bsonType: 'array',
                description: 'must be an array of slide elements',
                items: {
                  bsonType: 'object',
                  properties: {
                    type: { bsonType: 'string' },
                    id: { bsonType: 'string' },
                    left: { bsonType: ['double', 'int'] },
                    top: { bsonType: ['double', 'int'] },
                    width: { bsonType: ['double', 'int'] },
                    height: { bsonType: ['double', 'int'] },
                    content: { bsonType: 'string' }
                  }
                }
              },
              background: {
                bsonType: 'object',
                properties: {
                  type: { bsonType: 'string' },
                  color: { bsonType: 'string' }
                }
              }
            }
          }
        },
        createdAt: {
          bsonType: 'date',
          description: 'must be a date and is required'
        },
        updatedAt: {
          bsonType: 'date',
          description: 'must be a date and is required'
        }
      }
    }
  }
});

// Create indexes for better performance
db.presentations.createIndex({ "createdAt": 1 });
db.presentations.createIndex({ "title": "text" });

print('MongoDB initialization completed for presentation service');